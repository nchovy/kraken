#include <winsock2.h>
#include <ws2ipdef.h>
#include <windows.h>
#include <wininet.h>
#include <iphlpapi.h>
#include "resource.h"
#include "ShReg.h"

#pragma comment (lib, "iphlpapi.lib")

#define MALLOC(x) HeapAlloc(GetProcessHeap(), 0, (x))
#define FREE(x) HeapFree(GetProcessHeap(), 0, (x))

#define TRAY_NOTIFY (WM_APP + 100)
#define ID_LISTBOX 200
#define MAX_LOG 30

LRESULT CALLBACK WndProc(HWND,UINT,WPARAM,LPARAM);
HINSTANCE g_hInst;
HWND hWndMain;
LPCTSTR lpszClass=TEXT("Kraken Sleep Proxy");

//Dialog
HWND hDlgNetwork;// 네트워크 정보 창
HWND hList; // 네트워크 ListBox

//Registry 정보
TCHAR szPolicyUrl[128],szLogAddr[256],szGUID[40];
int nPolicyInterval, nHbInterval, nAway, nForceHibernate;


//HTTP Download
TCHAR szHttpUrl[128], szHttpDir[128];
int nHttpPort;
int dummy=1;

//Send Log
TCHAR szSendLogAddr[128];
int nSendLogPort;

//NIC 정보
TCHAR adaptername[5][128],macaddress[5][18],ipaddress[5][16],description[5][128];
int nicNum;

//Msg
TCHAR szMsg[1024];

//Log Message
TCHAR szLog[MAX_LOG][1024];
int countLog=0;

//Power Message
TCHAR szPwr[255];

//Agent 상태
BOOL busymode=true;
BOOL opendlg=FALSE;
BOOL isSuspend=false;
DWORD tickcount;

//소켓 연결
WSADATA wsaData;

int APIENTRY WinMain(HINSTANCE hInstance,HINSTANCE hPrevInstance
	  ,LPSTR lpszCmdParam,int nCmdShow)
{
	HWND hWnd;
	MSG Message;
	WNDCLASS WndClass;
	g_hInst=hInstance;

	WndClass.cbClsExtra=0;
	WndClass.cbWndExtra=0;
	WndClass.hbrBackground=(HBRUSH)(COLOR_WINDOW+1);
	WndClass.hCursor=LoadCursor(NULL,IDC_ARROW);
	WndClass.hIcon=LoadIcon(NULL,IDI_APPLICATION);
	WndClass.hInstance=hInstance;
	WndClass.lpfnWndProc=WndProc;
	WndClass.lpszClassName=lpszClass;
	WndClass.lpszMenuName=NULL;
	WndClass.style=CS_HREDRAW | CS_VREDRAW;
	RegisterClass(&WndClass);

	hWnd=CreateWindow(lpszClass,lpszClass,WS_OVERLAPPEDWINDOW,
		CW_USEDEFAULT,CW_USEDEFAULT,CW_USEDEFAULT,CW_USEDEFAULT,
		NULL,(HMENU)NULL,hInstance,NULL);
	//ShowWindow(hWnd,nCmdShow);

	while (GetMessage(&Message,NULL,0,0)) {
		TranslateMessage(&Message);
		DispatchMessage(&Message);
	}
	return (int)Message.wParam;
}
BOOL IsWindowsNT()
{
    OSVERSIONINFO verInfo; 
    verInfo.dwOSVersionInfoSize = sizeof(verInfo); 
    GetVersionEx(&verInfo) ;
    switch(verInfo.dwPlatformId) 
    { 
        case VER_PLATFORM_WIN32_WINDOWS: 
              return FALSE; 
        case VER_PLATFORM_WIN32_NT: 
              return TRUE;
     }
   return FALSE;
}

 
BOOL AdjustSystemForShutdown()
{
        if(IsWindowsNT() == FALSE)
               return FALSE ;

        HANDLE hTk;
        TOKEN_PRIVILEGES tp;

        /* Get a token for this process. */
        if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hTk))
                return FALSE;

        /* Get the LUID for the shutdown privilege. */
        LookupPrivilegeValue(NULL, SE_SHUTDOWN_NAME, &tp.Privileges[0].Luid);

        tp.PrivilegeCount = 1; /* one privilege to set */
        tp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

        /* Get the shutdown privilege for this process. */
        return AdjustTokenPrivileges(hTk, FALSE, &tp, 0, (PTOKEN_PRIVILEGES)NULL, 0);
}

///////////////////////////////////////////////////////////////////////
//char 에서 wchar_t 로의 형변환 함수
wchar_t* ConvertCtoWC(char* str)
{
    //wchar_t형 변수 선언
    wchar_t* pStr;
    //멀티 바이트 크기 계산 길이 반환
    int strSize = MultiByteToWideChar(CP_ACP, 0,str, -1, NULL, NULL);
    //wchar_t 메모리 할당
    pStr = new WCHAR[strSize];
    //형 변환
    MultiByteToWideChar(CP_ACP, 0,str, strlen(str)+1, pStr, strSize);
    return pStr;
}
///////////////////////////////////////////////////////////////////////
//wchar_t 에서 char 로의 형변환 함수
char * ConvertWCtoC(wchar_t* str)
{
    //반환할 char* 변수 선언
    char* pStr ;
    //입력받은 wchar_t 변수의 길이를 구함
    int strSize = WideCharToMultiByte(CP_ACP, 0,str,-1, NULL, 0,NULL, NULL);
    //char* 메모리 할당
    pStr = new char[strSize];
    //형 변환 
    WideCharToMultiByte(CP_ACP, 0, str, -1, pStr, strSize, 0,0);
    return pStr;
}
///////////////////////////////////////////////////////////////////////
//wchar_t 에서 int 로의 형변환 함수
int ConvertWCtoInt(wchar_t* str)
{
    //반환할 char* 변수 선언
    char* pStr ;
    //입력받은 wchar_t 변수의 길이를 구함
    int strSize = WideCharToMultiByte(CP_ACP, 0,str,-1, NULL, 0,NULL, NULL);
    //char* 메모리 할당
    pStr = new char[strSize];
    //형 변환 
    WideCharToMultiByte(CP_ACP, 0, str, -1, pStr, strSize, 0,0);
	//
    
	return atoi(pStr);
}
void GetNicInfo()
{
	PIP_ADAPTER_INFO pAdapterInfo;
	PIP_ADAPTER_INFO pAdapter = NULL;
	DWORD dwRetVal = 0;
	ULONG ulOutBufLen= sizeof (IP_ADAPTER_INFO);
	TCHAR strMac[50];
	int i=0;

	ulOutBufLen = sizeof(IP_ADAPTER_INFO);
	pAdapterInfo = (IP_ADAPTER_INFO *) malloc(sizeof (IP_ADAPTER_INFO));
    if (pAdapterInfo == NULL) {
        MessageBox(hWndMain, TEXT("pAdapterInfo - ERROR"), TEXT("ERROR"), MB_OK);
    }

	if (GetAdaptersInfo( pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW) {
		free(pAdapterInfo);
		pAdapterInfo = (IP_ADAPTER_INFO *) malloc ( ulOutBufLen );
		 if (pAdapterInfo == NULL) {
             MessageBox(hWndMain, TEXT("pAdapterInfo - ERROR"), TEXT("ERROR"), MB_OK);
        }
	}

	if ((dwRetVal = GetAdaptersInfo( pAdapterInfo, &ulOutBufLen)) == NO_ERROR) {
	  pAdapter = pAdapterInfo;
		while (pAdapter) {
			memset(strMac,0,50);
			wsprintf(strMac, TEXT("%0.2x:%0.2x:%0.2x:%0.2x:%0.2x:%0.2x"), pAdapter->Address[0],pAdapter->Address[1],pAdapter->Address[2],pAdapter->Address[3],pAdapter->Address[4],pAdapter->Address[5]);
			wcscpy_s(adaptername[i],128,ConvertCtoWC(pAdapter->AdapterName));
			wcscpy_s(macaddress[i],50,strMac);
			wcscpy_s(ipaddress[i],16, ConvertCtoWC(pAdapter->IpAddressList.IpAddress.String));
			wcscpy_s(description[i],128,ConvertCtoWC(pAdapter->Description));
			i++;
			pAdapter = pAdapter->Next;
			
		}
	}
	nicNum=i;
}
void GetRegistry()
{
	GUID tmpGuid;

/*	TCHAR szPolicyUrl[128],szLogAddr[256],szGUID[40];
int nPolicyInterval, nHbInterval, nAway, nForceHibernate;*/
	memset(szPolicyUrl,0,128);
	memset(szLogAddr,0,128);
	memset(szGUID,0,128);

	SHRegReadString(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("PolicyUrl"),TEXT("http://10.0.1.11:8080/ksp/policy"),szPolicyUrl,256);
	SHRegReadString(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("LogAddr"),TEXT("10.0.1.11:514"),szLogAddr,256);
	SHRegReadString(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("GUID"),TEXT("1"),szGUID,80);
	//MessageBox(hWndMain, szGUID, TEXT("GUID"), MB_OK);
	if (wcslen(szGUID)==1){
		CoCreateGuid(&tmpGuid);
		wsprintf(szGUID, TEXT("{%08x-%04x-%04x-%02x%02x-%02x%02x%02x%02x%02x%02x}"), tmpGuid.Data1, tmpGuid.Data2, tmpGuid.Data3, tmpGuid.Data4[0], tmpGuid.Data4[1], tmpGuid.Data4[2], tmpGuid.Data4[3], tmpGuid.Data4[4], tmpGuid.Data4[5], tmpGuid.Data4[6], tmpGuid.Data4[7]);
		//StringFromGUID2(tmpGuid,wGUID,40);
		//WideCharToMultiByte(CP_ACP, 0, wGUID, 40, szGUID, 40, NULL, NULL);
		SHRegWriteString(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("GUID"), szGUID);
	}
	nPolicyInterval=SHRegReadInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("PolicyInterval"),30);
	nHbInterval=SHRegReadInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("HeartbeatInterval"),60);
	nAway=SHRegReadInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("Away"),300);
	nForceHibernate=SHRegReadInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"),TEXT("Force_Hibernate"),0);

	// *** HTTP Connection Info****
	TCHAR seps1[] = TEXT("/");
	TCHAR seps2[] = TEXT(":");
	TCHAR *token;
	TCHAR tmpUrl[128] = {0};
	TCHAR tmpPort[6] = {0};
	TCHAR tmpLogAddr[128] = {0};
	TCHAR tmpLogPort[6] = {0};
	
	// Dir
	if(wcsrchr(szPolicyUrl + 7, TEXT('/')) != NULL){
		token = wcstok(szPolicyUrl + 7, seps1);
		if(token!=NULL)
			wcscpy(tmpUrl, token);
		token = wcstok(NULL, seps2);
		if(token!=NULL)
			wcscpy(szHttpDir, token);
	}
	else{
		wcscpy(tmpUrl, szPolicyUrl);
		wcscpy(szHttpDir, TEXT(""));
	}
	//Port
	if(wcsrchr(tmpUrl, TEXT(':')) != NULL){
		token = wcstok(tmpUrl, seps2);
		if(token!=NULL)
			wcscpy(szHttpUrl, tmpUrl);
		token = wcstok(NULL, seps2);
		if(token!=NULL){
			wcscpy(tmpPort, token);
			nHttpPort=ConvertWCtoInt(tmpPort);
		}
		else
			nHttpPort=80;
	}else{
		wcscpy(szHttpUrl, tmpUrl);
		nHttpPort=80;
	}
	
	// Log
	//TCHAR szSendLogAddr[128];
	//int nSendLogPort;
	if(wcsrchr(szLogAddr, TEXT(':')) != NULL){
		token = wcstok(szLogAddr, seps2);
		if(token!=NULL)
			wcscpy(szSendLogAddr, token);
		token = wcstok(NULL, seps2);
		if(token!=NULL){
			wcscpy(tmpLogPort, token);
			nSendLogPort=ConvertWCtoInt(tmpLogPort);		
		}
		else
			nSendLogPort=514;
	}else{
		wcscpy(szSendLogAddr, szLogAddr);
		nSendLogPort=514;
	}
	
	//MessageBox(hWndMain, tmpLogPort, TEXT("11"), MB_OK);
}

void MakeMsg(int msgtype)
{
	//시스템 정보
	TCHAR szComName[255];
	TCHAR szUserName[255];
	DWORD len;
	TCHAR   szDomain[255] = {0};
	DWORD   cbDomain = 255;
	TCHAR tmpInfo[512];
	int i;

	len=255;
	GetComputerName(szComName,&len);
	len=255;
	GetUserName(szUserName,&len);
	GetComputerNameEx(ComputerNameDnsDomain,szDomain,&cbDomain);

	memset(tmpInfo,0,512);
	for(i=0;i<nicNum;i++){
		wcscat_s(tmpInfo,macaddress[i]);
		wcscat_s(tmpInfo,TEXT(","));
		wcscat_s(tmpInfo,ipaddress[i]);
		wcscat_s(tmpInfo,TEXT(","));
		wcscat_s(tmpInfo,TEXT("\""));
		wcscat_s(tmpInfo,description[i]);
		wcscat_s(tmpInfo,TEXT("\""));
		if(i!=(nicNum-1))
			wcscat_s(tmpInfo,TEXT(","));
	}
	memset(szMsg,0,1024);
	wsprintf(szMsg,TEXT("<133>1,%d,%s,%s,%s,%s,%d,%s"),msgtype,szGUID,szUserName,szComName,szDomain,nicNum,tmpInfo);
	
}
void PowerStateMsg()
{
	SYSTEM_POWER_STATUS lpSystemPowerStatus={0};
	
	int h,m;

	GetSystemPowerStatus(&lpSystemPowerStatus);
	
	if(lpSystemPowerStatus.ACLineStatus)
		wsprintf(szPwr,TEXT("AC, Battery : %d%%"),lpSystemPowerStatus.BatteryLifePercent);
	else{
		if(lpSystemPowerStatus.BatteryLifeTime==-1)
			wsprintf(szPwr,TEXT("DC, Battery : %d%%, unknown left"), lpSystemPowerStatus.BatteryLifePercent);
		else{
			m = (int) (lpSystemPowerStatus.BatteryLifeTime / 60);
			h = (int) (m / 60);
			m = m % 60;
			wsprintf(szPwr,TEXT("DC, Battery : %d%%, %dh %dm left"), lpSystemPowerStatus.BatteryLifePercent, h,m);
		}
	}

}
void AddLog(int logtype)
{
	SYSTEMTIME st;
	GetLocalTime(&st);
	int i;
	TCHAR szTime[21];
	static int maxWidth=0;
	DWORD nListBoxLastItem;
	TCHAR aa[20];
	
	wsprintf(szTime,TEXT("%d-%0.2d-%0.2d %0.2d:%0.2d:%0.2d"),st.wYear,st.wMonth,st.wDay,st.wHour,st.wMinute,st.wSecond);
	if(countLog==MAX_LOG){
		for(i=0;i<MAX_LOG;i++)
			lstrcpy(szLog[i],szLog[i+1]);
		countLog--;
	}
	
	switch (logtype){
		case 1: //Away mode - Idle
			wsprintf(szLog[countLog],TEXT("%s - Idle(No Input for %ds)"),szTime, nAway);
			break;
		case 2: //Busy mode
			wsprintf(szLog[countLog],TEXT("%s - Busy"),szTime, nAway);
			break;
		case 3: //Suspend
			wsprintf(szLog[countLog],TEXT("%s - Suspend - %s"),szTime,szMsg);
			break;
		case 4: //Wake
			wsprintf(szLog[countLog],TEXT("%s - Wake up - %s"),szTime,szMsg);
			break;
		case 5: //Heartbeat
			wsprintf(szLog[countLog],TEXT("%s - Heartbeat - %s"),szTime,szMsg);
			break;
		case 6:
			wsprintf(szLog[countLog],TEXT("%s - Policy Download"), szTime);
			break;
		case 7://AC/DC, 배터리 잔량
			wsprintf(szLog[countLog],TEXT("%s - Power Source : %s"),szTime, szPwr);
			break;
	}
	if(maxWidth<(lstrlen(szLog[countLog])*6))
		maxWidth=lstrlen(szLog[countLog])*6;
	SendDlgItemMessage(hDlgNetwork,IDC_LIST2,LB_ADDSTRING,0,(LPARAM)szLog[countLog]);
	SendDlgItemMessage(hDlgNetwork,IDC_LIST2,LB_SETHORIZONTALEXTENT,maxWidth,0);//가로스크롤바
	nListBoxLastItem = GetListBoxInfo(GetDlgItem(hDlgNetwork, IDC_LIST2));
	SendDlgItemMessage(hDlgNetwork,IDC_LIST2,LB_SETCURSEL,(WPARAM)(nListBoxLastItem-1),0);
	countLog++;
}

void HTTPDown(HWND h)
{
	HINTERNET hInternet, hHttp;
	HINTERNET hReq;
	DWORD Size;
	TCHAR szObject[128];

	char buf[512];
	char buf2[2048]={0,}; //http download 정보 저장
	TCHAR buf3[2048]={0,};
	
	DWORD dwRead;

	hInternet=InternetOpen(TEXT("HTTPTEST"), INTERNET_OPEN_TYPE_PRECONFIG, NULL, NULL, 0);
	if (hInternet == NULL) { return;}
	hHttp=InternetConnect(hInternet,szHttpUrl,nHttpPort,TEXT(""),TEXT(""),INTERNET_SERVICE_HTTP,0,0);
	if (hHttp==NULL) return;

	wsprintf(szObject,TEXT("%s?GUID='%s'&dummy=%d"),szHttpDir,szGUID,dummy++);
	hReq=HttpOpenRequest(hHttp,TEXT("GET"),szObject,NULL,NULL,NULL,0,0);
	
	HttpSendRequest(hReq,NULL,0,NULL,0);

	do {
		InternetQueryDataAvailable(hReq,&Size,0,0);
		InternetReadFile(hReq,buf,Size,&dwRead);
		buf[dwRead]=0;
		strcat(buf2,buf);
	} while (dwRead != 0);
	
	//HTTP 연결 종료
	InternetCloseHandle(hHttp);
	InternetCloseHandle(hInternet);
	hHttp=NULL;
	hInternet=NULL;

	//Convert char to TCHAR
	MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED, buf2, strlen(buf2), buf3, 2048);
	
	// token
	TCHAR seps[]   = TEXT(" =\n");
	TCHAR *token;
	TCHAR tmp[16][128];
	//TCHAR tmpUrl[128], tmpAddr[128], tmpHeartbeatInterval[5], tmpAway[5], tmpForceHibernate[2];
	int unitcount=0,j;
	bool isPolicyChanged = false;

	token = wcstok(buf3, seps);
	while(token!=NULL)
	{
		//wsprintf(mmmm, TEXT("%s + %s"), mmmm, token);
		memset(tmp[unitcount],0,128);
		wcscpy(tmp[unitcount],token);
		token = wcstok(NULL, seps);
		unitcount++;
	}

	for(j=0;j<unitcount;j++){
		if(wcscmp(tmp[j],TEXT("url"))==0){
			j++;
			if((wcslen(tmp[j])>1)){
				SHRegWriteString(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("PolicyUrl"), tmp[j]);
				isPolicyChanged=true;
			}
		}
		else if(wcscmp(tmp[j],TEXT("interval"))==0){
			j++;
			if(ConvertWCtoInt(tmp[j])>0&&ConvertWCtoInt(tmp[j])!=nPolicyInterval){
				SHRegWriteInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("PolicyInterval"), ConvertWCtoInt(tmp[j]));
				isPolicyChanged=true;
				KillTimer(h,1);
				SetTimer(h,1,(ConvertWCtoInt(tmp[j])*1000),NULL); //HTTP Download
			}
		}
		else if(wcscmp(tmp[j],TEXT("addr"))==0){
			j++;
			if((wcslen(tmp[j])>1)){
				SHRegWriteString(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("LogAddr"), tmp[j]);
				isPolicyChanged=true;
			}
		}
		else if(wcscmp(tmp[j],TEXT("heartbeat_interval"))==0){
			j++;
			if(ConvertWCtoInt(tmp[j])>0&&ConvertWCtoInt(tmp[j])!=nHbInterval){
				SHRegWriteInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("HeartbeatInterval"), ConvertWCtoInt(tmp[j]));
				isPolicyChanged=true;
				KillTimer(h,2);
				SetTimer(h,2,(ConvertWCtoInt(tmp[j])*1000),NULL);
			}
		}
		else if(wcscmp(tmp[j],TEXT("away"))==0){
			j++;
			if(ConvertWCtoInt(tmp[j])>0){
				SHRegWriteInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("Away"), ConvertWCtoInt(tmp[j]));
				isPolicyChanged=true;
			}
		}
		else if(wcscmp(tmp[j],TEXT("force_hibernate"))==0){
			j++;
			if(wcsncmp(tmp[j],TEXT("true"),4)==0){
				SHRegWriteInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("Force_Hibernate"), 1);
				isPolicyChanged=true;
			}else if(wcsncmp(tmp[j],TEXT("false"),5)==0){
				SHRegWriteInt(HKEY_LOCAL_MACHINE,TEXT("Software\\KrakenSleepProxy"), TEXT("Force_Hibernate"), 0);
				isPolicyChanged=true;
			}
		}
	
	}
	TCHAR szPolicyMsg[512];
	if(isPolicyChanged){
		GetRegistry();
		//정책
		wsprintf(szPolicyMsg,TEXT("[GUID]\n%s\n[Policy]\nURL = %s\nInterval = %d\n[Power]\nAddress = %s\nHeartbeat Interval = %d\nAway Mode = %d\nForce Hibernate = %d"), szGUID, szPolicyUrl, nPolicyInterval, szLogAddr,nHbInterval,nAway, nForceHibernate);

		//CreateWindow(TEXT("static"),szPolicyMsg,WS_CHILD | WS_VISIBLE,10,10,600,100,hDlg,(HMENU)-1,g_hInst,NULL);
		SetDlgItemText(hDlgNetwork,IDC_STATIC1,szPolicyMsg);
		
	}
	//MessageBox(hWndMain, tmp[2], TEXT("TEST1"), MB_OK);

	// token 끝

}

void SendUDPMessage(int nMessageType){
	SOCKET hSocket;
	SOCKADDR_IN servAddr;
	int ret;
	
	//buffer
	char cbBuff[1024] = {0};

	hSocket=socket(PF_INET,SOCK_DGRAM,0);
	memset(&servAddr,0,sizeof(servAddr));
	servAddr.sin_family=AF_INET;
	servAddr.sin_addr.s_addr=inet_addr(ConvertWCtoC(szSendLogAddr));
	servAddr.sin_port=htons(nSendLogPort);

	ret = connect(hSocket, (sockaddr*)&servAddr, sizeof(servAddr));
	if (ret == SOCKET_ERROR) {
		int i = 1;
		MessageBox(hWndMain,TEXT("Error"), TEXT("Error"),MB_OK);
	}
	MakeMsg(nMessageType);
	AddLog(nMessageType);
	memset(cbBuff,0,1024);
	WideCharToMultiByte(CP_UTF8, 0, szMsg, wcslen(szMsg), cbBuff, sizeof(cbBuff), NULL, NULL);
	send(hSocket, cbBuff, strlen(cbBuff),0);

	//소켓 종료
	closesocket(hSocket);
}
BOOL CALLBACK NetworkDlgProc(HWND hDlg, UINT iMessage, WPARAM wParam, LPARAM lParam)
{

	TCHAR szPolicyMsg[512];
	//adapters..
	int ni,li;
	
	switch (iMessage){
		case WM_INITDIALOG:
			opendlg=TRUE;
			//정책
			wsprintf(szPolicyMsg,TEXT("[GUID]\n%s\n[Policy]\nURL = %s\nInterval = %d\n[Power]\nAddress = %s\nHeartbeat Interval = %d\nAway Mode = %d\nForce Hibernate = %d"), szGUID, szPolicyUrl, nPolicyInterval, szLogAddr,nHbInterval,nAway, nForceHibernate);

			//CreateWindow(TEXT("static"),szPolicyMsg,WS_CHILD | WS_VISIBLE,10,10,600,100,hDlg,(HMENU)-1,g_hInst,NULL);
			SetDlgItemText(hDlg,IDC_STATIC1,szPolicyMsg);
			for(ni=0;ni<nicNum;ni++){
				SendDlgItemMessage(hDlg,IDC_LIST1,LB_ADDSTRING,0,(LPARAM)adaptername[ni]);
				SendDlgItemMessage(hDlg,IDC_LIST1,LB_ADDSTRING,0,(LPARAM)macaddress[ni]);
				SendDlgItemMessage(hDlg,IDC_LIST1,LB_ADDSTRING,0,(LPARAM)ipaddress[ni]);
				SendDlgItemMessage(hDlg,IDC_LIST1,LB_ADDSTRING,0,(LPARAM)description[ni]);
				SendDlgItemMessage(hDlg,IDC_LIST1,LB_ADDSTRING,0,(LPARAM)TEXT("-----------------------------------------------------------------------------------------------------------------------------------------------"));
			}
			for(li=0;li<countLog;li++){
				SendDlgItemMessage(hDlg,IDC_LIST2,LB_ADDSTRING,0,(LPARAM)szLog[li]);
				SendDlgItemMessage(hDlg,IDC_LIST2,LB_SETCURSEL,(WPARAM)(countLog-1),0);
			}

			hDlgNetwork=hDlg;
			
			return TRUE;
		case WM_COMMAND:
			switch(LOWORD(wParam)){
				case IDOK:
				case IDCANCEL:
					opendlg=FALSE;
					EndDialog(hDlg,IDOK);
					return TRUE;
			}
			return FALSE;
	}
	return FALSE;
}


LRESULT CALLBACK WndProc(HWND hWnd,UINT iMessage,WPARAM wParam,LPARAM lParam)
{
	HDC hdc;
	PAINTSTRUCT ps;
	NOTIFYICONDATA nid;
	HMENU hMenu, hPopupMenu;
	POINT pt;

	//마지막 입력 확인
	LASTINPUTINFO LastInput;
	LastInput.dwTime = 0;
	LastInput.cbSize = sizeof(LASTINPUTINFO);

	//internet connection check
	DWORD dwConnectionTypes;
	int nInternetCheckCount;

	//DWORD a =0;
	switch (iMessage) {
		case WM_CREATE:
			tickcount=GetTickCount();
			//Tray
			ZeroMemory(&nid, sizeof(nid));
			nid.cbSize = sizeof(NOTIFYICONDATA);
			nid.hWnd =hWnd;
			nid.uID=0;
			nid.uFlags = NIF_ICON | NIF_TIP | NIF_MESSAGE;
			nid.uCallbackMessage = TRAY_NOTIFY;
			nid.hIcon = LoadIcon(g_hInst, MAKEINTRESOURCE(IDI_ICON1));
			lstrcpy(nid.szTip, TEXT("Kraken Sleep Proxy"));
			Shell_NotifyIcon(NIM_ADD, &nid);

			//nic 정보 수집
			GetNicInfo();
			
			//registry정보 읽어오기
			GetRegistry();

			//Timer 설정
			SetTimer(hWnd,1,(nPolicyInterval*1000),NULL); //HTTP Download
			SetTimer(hWnd,2,(nHbInterval*1000),NULL); //UDP
			SetTimer(hWnd,3,1000,NULL); //Last Input Check
			SetTimer(hWnd,4,5000,NULL); //Power check
			
			//html Download
			if(InternetGetConnectedState(&dwConnectionTypes,0))
				HTTPDown(hWnd);

			//udp Socket 설정
			WSAStartup(MAKEWORD(2,2), &wsaData);
			
			SendUDPMessage(5);
			return 0;
		case TRAY_NOTIFY:
			switch(lParam){
				case WM_RBUTTONDOWN:
					hMenu=LoadMenu(g_hInst, MAKEINTRESOURCE(IDR_MENU1));
					hPopupMenu=GetSubMenu(hMenu,0);
					GetCursorPos(&pt);
					SetForegroundWindow(hWnd);
					TrackPopupMenu(hPopupMenu, TPM_LEFTALIGN|TPM_LEFTBUTTON | TPM_RIGHTBUTTON, pt.x, pt.y, 0, hWnd, NULL);
					SetForegroundWindow(hWnd);
					DestroyMenu(hPopupMenu);
					DestroyMenu(hMenu);
					break;
				case WM_LBUTTONDOWN:
					if (opendlg==FALSE)
						DialogBox(g_hInst,MAKEINTRESOURCE(IDD_DIALOG1),HWND_DESKTOP,NetworkDlgProc);
					break;

			}
			return 0;
		case WM_TIMER:
			switch(wParam){
				case 1:
					if(InternetGetConnectedState(&dwConnectionTypes,0)){
						if(busymode){
							HTTPDown(hWnd);
							AddLog(6);
						}
					}
					break;
				case 2:  //heartbeat
					if(busymode){
						SendUDPMessage(5);
					}
					break;
				case 3:
					if(GetLastInputInfo(&LastInput)){
						if(busymode){
							if(GetTickCount() - tickcount > nAway*1000){
								if(GetTickCount() - LastInput.dwTime > nAway*1000) // Away값만큼 동안 입력이 없을 경우
								{
									if(nForceHibernate==0){
										SendUDPMessage(1);
										busymode=false;
									}else{
										if(AdjustSystemForShutdown()){
											SendUDPMessage(3);
											Sleep(1000);
											SetSystemPowerState(TRUE,TRUE);
										}
									}
								}
							}
								
						}else{
							if(!isSuspend){
								if(GetTickCount() - LastInput.dwTime < 10000){
									busymode=true;
									SendUDPMessage(2);
								}
							}
						}
					}
					break;
				case 4:
					if(busymode){
						PowerStateMsg();
						AddLog(7);
					}
					break;
			}
			return 0;
		case WM_POWERBROADCAST:
			switch(LOWORD(wParam)){
				case PBT_APMSUSPEND://suspend
					busymode=false;
					isSuspend=true;
					SendUDPMessage(3);
					Sleep(1000);
					//소켓 종료
					//closesocket(hSocket);
					//WSACleanup();
					break;
				case PBT_APMRESUMESUSPEND://resume
					tickcount=GetTickCount();
					busymode=true;
					isSuspend=false;
					
					nInternetCheckCount=0;
					Sleep(10000);
					while(1){
						if(InternetGetConnectedState(&dwConnectionTypes,0)){
							SendUDPMessage(4);					
							break;
						}
						Sleep(1000);
						if(nInternetCheckCount++==30)
							break;
					}
					break;
			}
			return 0;
		case WM_COMMAND:
			switch(LOWORD(wParam)){
				case IDM_NETWORK:
					if(opendlg==FALSE)
						DialogBox(g_hInst,MAKEINTRESOURCE(IDD_DIALOG1),HWND_DESKTOP,NetworkDlgProc);
					break;
				case IDM_ABOUT:
					MessageBox(hWnd, TEXT("Kraken Sleep Proxy"), TEXT("프로그램 소개"), MB_OK);
					break;
				case IDM_EXIT:
					DestroyWindow(hWnd);
					break;
			}
			return 0;
		case WM_PAINT:
			hdc=BeginPaint(hWnd,&ps);
			EndPaint(hWnd,&ps);
			return 0;
		case WM_DESTROY:
			//Timer 종료
			KillTimer(hWnd,1);
			KillTimer(hWnd,2);
			KillTimer(hWnd,3);
			KillTimer(hWnd,4);
			
			PostQuitMessage(0);

			WSACleanup();
			return 0;
	}
	return(DefWindowProc(hWnd,iMessage,wParam,lParam));
}