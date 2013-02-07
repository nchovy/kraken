/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifdef WIN32
  #include <Winsock2.h>
  #include <IPHlpApi.h>
  #include <windows.h>
#endif
#ifdef __linux__
  #include <string.h>
  #include <sys/ioctl.h>
  #include <sys/types.h>
  #include <sys/socket.h>
  #include <linux/if.h>
  #include <netinet/in.h>
#endif
#ifdef __APPLE__
  #include <string.h>
  #include <sys/ioctl.h>
  #include <sys/types.h>
  #include <sys/socket.h>
  #include <netinet/in.h>
  #include <net/if.h>
  #include <net/if_dl.h>
  #include <ifaddrs.h>
#endif
#include <stdlib.h>
#include <pcap.h>
#include "org_krakenapps_pcap_live_PcapDevice.h"

#define MAX_NUMBER_OF_INSTANCE 255L

int checkDeviceStatus(JNIEnv *, int);
#if defined(__linux__) || defined (__APPLE__)
  int GetTickCount();
#endif


pcap_t *pcds[MAX_NUMBER_OF_INSTANCE] = {0, };
struct bpf_program *fp[MAX_NUMBER_OF_INSTANCE] = {0, };
int t_limit[MAX_NUMBER_OF_INSTANCE] = {0, };
volatile int getPacketFlag[MAX_NUMBER_OF_INSTANCE] = {0, };


// class PcapDevice

JNIEXPORT jobjectArray JNICALL Java_org_krakenapps_pcap_live_PcapDeviceManager_getDeviceList(JNIEnv *env, jclass cls) {
	jclass clzPcapDeviceMetadata = (*env)->FindClass(env, "org/krakenapps/pcap/live/PcapDeviceMetadata");
	jmethodID clzPcapDevice_init = (*env)->GetMethodID(env, clzPcapDeviceMetadata, "<init>", "(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;[B[Lorg/krakenapps/pcap/live/AddressBinding;[BI)V");
	pcap_if_t *alldevs;
	pcap_if_t *dev;
	char errbuf[PCAP_ERRBUF_SIZE];
	jobjectArray devices = NULL;
#ifdef WIN32
	PIP_INTERFACE_INFO pInfo;
	u_long lSize = 0;
	char **devNames;
	int ret;
#endif
#if defined(__linux__)
	struct ifreq ifr;
	int s;
#endif
#if defined (__APPLE__)
    pcap_addr_t *ifa_iter;
    jbyte *ifa_addr;
#endif
	int i;

	if(pcap_findalldevs(&alldevs, errbuf) == -1) {
		fprintf(stderr,"Error in pcap_findalldevs: %s\n", errbuf);
		return NULL;
	}

#ifdef WIN32
	GetInterfaceInfo(NULL, &lSize);
	pInfo = (PIP_INTERFACE_INFO)malloc(lSize);

	ret = GetInterfaceInfo(pInfo, &lSize);
	if(ret != NO_ERROR) {
		if(ret == ERROR_NO_DATA) {
			devices = (*env)->NewObjectArray(env, 0, clzPcapDeviceMetadata, NULL);
			return devices;
		} else if(ret == ERROR_NOT_SUPPORTED)
			fprintf(stderr, "OS not support GetInterfaceInfo\n");
		else
			fprintf(stderr, "Error in GetInterfaceInfo\n");
		return NULL;
	}

	devNames = (char **)malloc( sizeof(char *) * pInfo->NumAdapters );
	memset(devNames, 0, sizeof(char *) * pInfo->NumAdapters);

	for(i=0; i<pInfo->NumAdapters; i++) {
		lSize = WideCharToMultiByte(0, 0, pInfo->Adapter[i].Name, -1, NULL, 0, NULL, NULL);
		devNames[i] = (char *)malloc(lSize);
		WideCharToMultiByte(0, 0, pInfo->Adapter[i].Name, -1, devNames[i], lSize, NULL, NULL);
	}
#endif
#if defined(__linux__)
	s = socket(PF_INET, SOCK_DGRAM, 0);
#endif

	for(dev=alldevs, i=0; dev; dev=dev->next, i++);
	devices = (*env)->NewObjectArray(env, (jsize)i, clzPcapDeviceMetadata, NULL);

	for(dev=alldevs, i=0; dev; dev=dev->next, i++) {
#ifdef WIN32
		int offset = 12;
#else
		int offset = 0;
#endif

		jobject device = NULL;
		jstring name = (*env)->NewStringUTF(env, dev->name + offset);
		jstring description = (*env)->NewStringUTF(env, dev->description);
		jboolean loopback = (dev->flags & PCAP_IF_LOOPBACK) ? JNI_TRUE : JNI_FALSE;
		jstring dlinkName = NULL;
		jstring dlinkDesc = NULL;
		jbyteArray macaddr = (*env)->NewByteArray(env, 6);
		jobjectArray bindings = getAddressBindings(env, dev);
		bpf_u_int32 netp;
		bpf_u_int32 maskp;
		jbyteArray subnet = (*env)->NewByteArray(env, 4);
		jint netPrefixLength = 0;
		int j, k;

		// get datalink
		{
			pcap_t *tmp_dev = pcap_open_live(dev->name, 0, 0, 1000, errbuf);

			if(tmp_dev != NULL) {
				int linktype = pcap_datalink(tmp_dev);
				dlinkName = (*env)->NewStringUTF(env, pcap_datalink_val_to_name(linktype));
				dlinkDesc = (*env)->NewStringUTF(env, pcap_datalink_val_to_description(linktype));
				pcap_close(tmp_dev);
			}
		}

		// get MacAddress
#ifdef WIN32
		{
			char *p1, *p2, *p3;

			p1 = dev->name;
			while(*p1 != '{')
				p1++;

			for(j=0; j<pInfo->NumAdapters; j++) {
				MIB_IFROW mibIfRow;

				p2 = p1;
				p3 = devNames[j];
				while(*p3 != '{')
					p3++;

				if(strcmp(p2, p3) != 0)
					continue;

				mibIfRow.dwIndex = pInfo->Adapter[j].Index;
				GetIfEntry(&mibIfRow);
				(*env)->SetByteArrayRegion(env, macaddr, 0, mibIfRow.dwPhysAddrLen, mibIfRow.bPhysAddr);
			}
		}
#endif
#if defined(__linux__)
		memset(&ifr, 0x00, sizeof(ifr));
		strcpy(ifr.ifr_name, dev->name);
		ioctl(s, SIOCGIFHWADDR, &ifr);

		(*env)->SetByteArrayRegion(env, macaddr, 0, (jsize) 6, (jbyte *)ifr.ifr_hwaddr.sa_data);
#endif
#if defined(__APPLE__)
		for (ifa_iter = dev->addresses; ifa_iter != NULL; ifa_iter = ifa_iter->next) 
		{
			if(ifa_iter->addr->sa_family == AF_LINK && ifa_iter->addr->sa_data != NULL)
			{
				if (((struct sockaddr_dl*)ifa_iter->addr->sa_data)->sdl_alen > 6) 
				{
					ifa_addr = (jbyte *)LLADDR((struct sockaddr_dl*)ifa_iter->addr->sa_data)  + 1;
				}
				else
				{
					ifa_addr = (jbyte *)LLADDR((struct sockaddr_dl*)ifa_iter->addr->sa_data);
				}
				(*env)->SetByteArrayRegion(env, macaddr, 0, (jsize) 6, ifa_addr);
			}
		}
#endif

		// get Subnet
		if(pcap_lookupnet(dev->name, &netp, &maskp, errbuf) == -1)
			fprintf(stderr, "Error in pcap_lookupnet: %s\n", errbuf);

		for(j=0; j<4; j++) {
			jbyte *buf = (jbyte*)&netp + j;
			(*env)->SetByteArrayRegion(env, subnet, j, 1, buf);
		}

		// get NetworkPrefixLength
		for(j=0,k=1; j<32; j++,k<<=1) {
			if((maskp & k) != 0)
				netPrefixLength++;
		}

		if(description == NULL)
			description = (*env)->NewStringUTF(env, "");

		device = (*env)->NewObject(env, clzPcapDeviceMetadata, clzPcapDevice_init, name, description, loopback, dlinkName, dlinkDesc, macaddr, bindings, subnet, netPrefixLength);
		(*env)->SetObjectArrayElement(env, devices, (jsize)i, device);
	}

#ifdef WIN32
	for(i=0; i<pInfo->NumAdapters; i++)
		free(devNames[i]);
	free(devNames);
	free(pInfo);
#endif
	pcap_freealldevs(alldevs);

	return devices;
}

JNIEXPORT void JNICALL Java_org_krakenapps_pcap_live_PcapDevice_open(JNIEnv *env, jobject obj, jint id, jstring name, jint snaplen, jboolean promisc, jint milliseconds) {
	char errbuf[PCAP_ERRBUF_SIZE];
	const char *devName = NULL;
	char deviceName[512];

	if(pcds[id] != NULL) {
		jclass cIOException = (*env)->FindClass(env, "java/io/IOException");
		(*env)->ThrowNew(env, cIOException, "Device ID is already used");
		return;
	}
	devName = (const char *)(*env)->GetStringUTFChars(env, name, JNI_FALSE);

#ifdef WIN32
	sprintf_s(deviceName, sizeof(deviceName), "\\Device\\NPF_%s", devName);
#else
	snprintf(deviceName, sizeof(deviceName)-1, "%s", devName);
#endif

	pcds[id] = pcap_open_live(deviceName, snaplen, (promisc == JNI_TRUE ? 1 : 0), 100, errbuf);
	t_limit[id] = milliseconds;

	(*env)->ReleaseStringUTFChars(env, name, devName);

	if(pcds[id] == NULL)
		fprintf(stderr, "Error in pcap_open_live: %s\n", errbuf);
}

JNIEXPORT jobject JNICALL Java_org_krakenapps_pcap_live_PcapDevice_getPacket(JNIEnv *env, jobject obj, jint id) {
	struct pcap_pkthdr *pkt_header = NULL;
	const u_char *pkt_data = NULL;
	jboolean isNonblock;
	int startTime = (int)GetTickCount();
	
	if(checkDeviceStatus(env, id) == -1) return NULL;

	pkt_header = (struct pcap_pkthdr *)malloc( sizeof(struct pcap_pkthdr) );
	isNonblock = Java_org_krakenapps_pcap_live_PcapDevice_isNonblock(env, obj, id);

	if(isNonblock)
		pkt_data = pcap_next(pcds[id], pkt_header);
	else {
		while(pkt_data == NULL && (int)GetTickCount() - startTime < t_limit[id]) {
			if(pcds[id] == NULL) {
				jclass cIOException = (*env)->FindClass(env, "java/io/IOException");
				(*env)->ThrowNew(env, cIOException, "Device is not opened");
				return NULL;
			}
			getPacketFlag[id] = 1;
			pkt_data = pcap_next(pcds[id], pkt_header);
			getPacketFlag[id] = 0;
		}
	}

	if(pkt_data == NULL) {
		if(isNonblock)
			return NULL;
		else {
			jclass cIOException = (*env)->FindClass(env, "java/io/IOException");
			(*env)->ThrowNew(env, cIOException, "Timeout");
			return NULL;
		}
	}

	return makePacket(env, pkt_header, pkt_data);
}

JNIEXPORT void JNICALL Java_org_krakenapps_pcap_live_PcapDevice_write(JNIEnv *env, jobject obj, jint id, jbyteArray packet, jint offset, jint length) {
	jbyte *pac = NULL;

	if(checkDeviceStatus(env, id) == -1) return;

	pac = (*env)->GetByteArrayElements(env, packet, NULL);

	if(pcap_sendpacket(pcds[id], (const u_char*) pac+offset, length) == -1)
		fprintf(stderr, "Error in pcap_sendpacket\n");

	(*env)->ReleaseByteArrayElements(env, packet, pac, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_org_krakenapps_pcap_live_PcapDevice_setNonblock(JNIEnv *env, jobject obj, jint id, jint nonblock) {
	char errbuf[PCAP_ERRBUF_SIZE];

	if(checkDeviceStatus(env, id) == -1) return;

	if(pcap_setnonblock(pcds[id], nonblock, errbuf) == -1)
		fprintf(stderr, "Error in pcap_setnonblock: %s\n", errbuf);
}

JNIEXPORT jboolean JNICALL Java_org_krakenapps_pcap_live_PcapDevice_isNonblock(JNIEnv *env, jobject obj, jint id) {
	char errbuf[PCAP_ERRBUF_SIZE];
	int status;

	if(checkDeviceStatus(env, id) == -1) return JNI_FALSE;

	if((status = pcap_getnonblock(pcds[id], errbuf)) == -1)
		fprintf(stderr, "Error in pcap_getnonblock: %s\n", errbuf);

	if(status == 0)
		return JNI_FALSE;
	else
		return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_org_krakenapps_pcap_live_PcapDevice_setFilter(JNIEnv *env, jobject obj, jint id, jstring filter, jint optimize, jint netmask) {
	char *filt = NULL;

	if(checkDeviceStatus(env, id) == -1) return;

	if (fp[id] != NULL)	
		pcap_freecode(fp[id]);

	fp[id] = (struct bpf_program *)malloc( sizeof(struct bpf_program) );

	filt = (char*)(*env)->GetStringUTFChars(env, filter, JNI_FALSE);
	if(pcap_compile(pcds[id], fp[id], filt, optimize, netmask) == -1) {
		jclass clzException = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
		(*env)->ThrowNew(env, clzException, pcap_geterr(pcds[id]));
	} else {
		if(pcap_setfilter(pcds[id], fp[id]) == -1)
			fprintf(stderr, "Error in pcap_setfilter: %s\n", pcap_geterr(pcds[id]));
	}

	(*env)->ReleaseStringUTFChars(env, filter, filt);
}

JNIEXPORT jobject JNICALL Java_org_krakenapps_pcap_live_PcapDevice_getStat(JNIEnv *env, jobject obj, jint id) {
	jclass cPcapStat = (*env)->FindClass(env, "org/krakenapps/pcap/live/PcapStat");
	jmethodID statMetId = (*env)->GetMethodID(env, cPcapStat, "<init>", "(III)V");
	struct pcap_stat *ps = NULL;
	
	if(checkDeviceStatus(env, id) == -1) return NULL;
	ps = (struct pcap_stat *)malloc( sizeof(struct pcap_stat) );

	if(pcap_stats(pcds[id], ps) == -1) {
		fprintf(stderr, "Error in pcap_stat\n");
		return NULL;
	}

	return (*env)->NewObject(env, cPcapStat, statMetId, ps->ps_recv, ps->ps_drop, ps->ps_ifdrop);
}

JNIEXPORT void JNICALL Java_org_krakenapps_pcap_live_PcapDevice_close(JNIEnv *env, jobject obj, jint id) {
	pcap_t *temp = pcds[id];

	if(checkDeviceStatus(env, id) == -1) return;

	pcds[id] = NULL;
	while(getPacketFlag[id]);

	pcap_close(temp);
	if(fp[id] != NULL) {
		pcap_freecode(fp[id]);
		fp[id] = NULL;
	}
}

JNIEXPORT jobject JNICALL Java_org_krakenapps_pcap_live_PcapDevice_getPcapLibVersion(JNIEnv *env, jclass cls) {
	return (*env)->NewStringUTF(env, pcap_lib_version());
}


// Methods

jobjectArray getAddressBindings(JNIEnv *env, pcap_if_t *dev) {
	jclass clzAddressBinding = (*env)->FindClass(env, "org/krakenapps/pcap/live/AddressBinding");
	jobjectArray addresses = NULL;
	pcap_addr_t *ad;
	jmethodID addrMetId = (*env)->GetMethodID(env, clzAddressBinding, "<init>", "([B[B[B[B)V");
	int i;

	for(i=0, ad=dev->addresses; ad; ad=ad->next, i++);
	addresses = (*env)->NewObjectArray(env, i, clzAddressBinding, NULL);

	for(i=0, ad=dev->addresses; ad; ad=ad->next, i++) {
		jbyteArray addr = getAddress(env, ad->addr);
		jbyteArray subnet = getAddress(env, ad->netmask);
		jbyteArray broadcast = getAddress(env, ad->broadaddr);
		jbyteArray dest = getAddress(env, ad->dstaddr);
		jobject address = NULL;
		
		if(addr == NULL)
			continue;

		address = (*env)->NewObject(env, clzAddressBinding, addrMetId, addr, subnet, broadcast, dest);
		(*env)->SetObjectArrayElement(env, addresses, i, address);
	}

	return addresses;
}

jbyteArray getAddress(JNIEnv *env, struct sockaddr *addr) {
	jbyteArray bArray = NULL;

	if(addr == NULL)
		return NULL;

	switch(addr->sa_family) {
	case AF_INET:
		bArray = (*env)->NewByteArray(env, 4);
		(*env)->SetByteArrayRegion(env, bArray, 0, 4, (jbyte *)&((struct sockaddr_in *)addr)->sin_addr);
		break;

	case AF_INET6:
		bArray = (*env)->NewByteArray(env, 16);
		(*env)->SetByteArrayRegion(env, bArray, 0, 16, (jbyte *)&((struct sockaddr_in6 *)addr)->sin6_addr);
		break;
	}

	return bArray;
}


jobject makePacket(JNIEnv *env, struct pcap_pkthdr *header, const u_char *data) {
	jclass cPacket = (*env)->FindClass(env, "org/krakenapps/pcap/packet/PcapPacket");
	jmethodID pacMetId = (*env)->GetMethodID(env, cPacket, "<init>", "(Lorg/krakenapps/pcap/packet/PacketHeader;Lorg/krakenapps/pcap/packet/PacketPayload;)V");
	jobject packetHeader = makePacketHeader(env, header);
	jobject packetPayload = makePacketPayload(env, data, header->caplen);
	jobject packet = (*env)->NewObject(env, cPacket, pacMetId, packetHeader, packetPayload);

	return packet;
}

jobject makePacketHeader(JNIEnv *env, struct pcap_pkthdr *header) {
	jclass cPacketHeader = (*env)->FindClass(env, "org/krakenapps/pcap/packet/PacketHeader");
	jmethodID headMetId = (*env)->GetMethodID(env, cPacketHeader, "<init>", "(IIII)V");

	jint tsSec = header->ts.tv_sec;
	jint tsUsec = header->ts.tv_usec;
	jint inclLen = header->caplen;
	jint origLen = header->len;

	jobject packetHeader = (*env)->NewObject(env, cPacketHeader, headMetId, tsSec, tsUsec, inclLen, origLen);

	return packetHeader;
}

jobject makePacketPayload(JNIEnv *env, const u_char *data, jint inclLen) {
	jclass cPacketPayload = (*env)->FindClass(env, "org/krakenapps/pcap/packet/PacketPayload");
	jmethodID bodyMetId = (*env)->GetMethodID(env, cPacketPayload, "<init>", "([B)V");

	jbyteArray bytes = (*env)->NewByteArray(env, inclLen);
	jobject packetPayload = NULL;

	(*env)->SetByteArrayRegion(env, bytes, 0, inclLen, (jbyte *)data);
	packetPayload = (*env)->NewObject(env, cPacketPayload, bodyMetId, bytes);

	return packetPayload;
}

int checkDeviceStatus(JNIEnv *env, int id) {
	if(id == -1 || pcds[id] == NULL) {
		jclass cIOException = (*env)->FindClass(env, "java/io/IOException");
		(*env)->ThrowNew(env, cIOException, "Device is not opened");
		return -1;
	}

	return 0;
}

#if defined(__linux__) || defined (__APPLE__)
int GetTickCount() {
	struct timeval tv;

	gettimeofday(&tv, NULL);

	return tv.tv_usec;
}
#endif
