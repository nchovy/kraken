package org.krakenapps.socialweb.facebook.graphapi.objectcode;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCode {
	/*
	 * Convention
	 * 
	 * "errortype"_"Error Name"
	 * 	
	 * The Error name of "Front end error" is consisted of upper initial case and lower cases 
	 * The Error name of "Back end error" is consisted of upper case only and bonded with under bar  
	 *   
	 */
	General_API_EC_SUCCESS(0),	// Success	 
	General_API_EC_UNKNOWN(1),//	 An unknown error occurred	 
	General_API_EC_SERVICE(2),	// Service temporarily unavailable	
	General_API_EC_METHOD(3),	// Unknown method	
	General_API_EC_TOO_MANY_CALLS(4),	// Application request limit reached
	General_API_EC_BAD_IP(5),	// Unauthorized source IP address	 
	General_API_EC_HOST_API(6),	// This method must run on api.facebook.com
	General_API_EC_HOST_UP(7),	// This method must run on api-video.facebook.com	
	General_API_EC_SECURE(8),	// This method requires an HTTPS connection	
	General_API_EC_RATE(9),	// User is performing too many actions	
	General_API_EC_PERMISSION_DENIED(10),	// Application does not have permission for this action	
	General_API_EC_DEPRECATED(11),	// This method is deprecated	
	General_API_EC_VERSION(12),	 //This API version is deprecated	
	General_API_EC_INTERNAL_FQL_ERROR(13),	 //The underlying FQL query made by this API call has encountered an error. Please check that your parameters are correct.	
	General_API_EC_HOST_PUP(14),	 //This method must run on api-photo.facebook.com	
	General_API_EC_SESSION_SECRET_NOT_ALLOWED(15),	 //This method call must be signed with the application secret (You are probably calling a secure method using a session secret)	
	General_API_EC_HOST_READONLY(16),	// This method cannot be run on this host, which only supports read-only calls	
	General_API_EC_USER_TOO_MANY_CALLS(17),	// User request limit reached	
	General_API_EC_REQUEST_RESOURCES_EXCEEDED(18),	 //This API call could not be completed due to resource limits
	
	Parameter_API_EC_PARAM(100),//	 	 Invalid parameter	 
	Parameter_API_EC_PARAM_API_KEY(101),	// Invalid API key	 
	Parameter_API_EC_PARAM_SESSION_KEY(102),	 //Session key invalid or no longer valid	 
	Parameter_API_EC_PARAM_CALL_ID(103),	 //Call_id must be greater than previous	
	Parameter_API_EC_PARAM_SIGNATURE(104),	 //Incorrect signature	 
	Parameter_API_EC_PARAM_TOO_MANY(105),	 //The number of parameters exceeded the maximum for this operation	
	Parameter_API_EC_PARAM_USER_ID(110),	 //Invalid user id	 photos.addTag
	Parameter_API_EC_PARAM_USER_FIELD(111),	 //Invalid user info field	
	Parameter_API_EC_PARAM_SOCIAL_FIELD(112),	 //Invalid user field	
	Parameter_API_EC_PARAM_EMAIL(113),	 //Invalid email	
	Parameter_API_EC_PARAM_USER_ID_LIST(114),	 //Invalid user ID list	
	Parameter_API_EC_PARAM_FIELD_LIST(115),	 //Invalid field list	
	Parameter_API_EC_PARAM_ALBUM_ID(120),	// Invalid album id	
	Parameter_API_EC_PARAM_PHOTO_ID(121),	// Invalid photo id	
	Parameter_API_EC_PARAM_FEED_PRIORITY(130),	// Invalid feed publication priority	
	Parameter_API_EC_PARAM_CATEGORY(140),	 //Invalid category	
	Parameter_API_EC_PARAM_SUBCATEGORY(141),	 //Invalid subcategory	
	Parameter_API_EC_PARAM_TITLE(142),	 //Invalid title	
	Parameter_API_EC_PARAM_DESCRIPTION(143),	 //Invalid description	
	Parameter_API_EC_PARAM_BAD_JSON(144),	// Malformed JSON string	
	Parameter_API_EC_PARAM_BAD_EID(150),	// Invalid eid	
	Parameter_API_EC_PARAM_UNKNOWN_CITY(151),	 //Unknown city	
	Parameter_API_EC_PARAM_BAD_PAGE_TYPE(152),	 //Invalid page type	
	Parameter_API_EC_PARAM_BAD_LOCALE(170),	 //Invalid locale	
	Parameter_API_EC_PARAM_BLOCKED_NOTIFICATION(180),	 //This notification was not delieved	
	Parameter_API_EC_PARAM_ACCESS_TOKEN(190),	 //Invalid OAuth 2.0 Access Token
	
	UserPermission_API_EC_PERMISSION(200), //	 	 Permissions error	
	UserPermission_API_EC_PERMISSION_USER(210), //	 User not visible	
	UserPermission_API_EC_PERMISSION_NO_DEVELOPERS(211),	 //Application has no developers.	 admin.setAppProperties
	UserPermission_API_EC_PERMISSION_OFFLINE_ACCESS(212),	 //Renewing a session offline requires the extended permission offline_access	
	UserPermission_API_EC_PERMISSION_ALBUM(220),	// Album or albums not visible	
	UserPermission_API_EC_PERMISSION_PHOTO(221),	// Photo not visible	
	UserPermission_API_EC_PERMISSION_MESSAGE(230),	 //Permissions disallow message to user	
	UserPermission_API_EC_PERMISSION_MARKUP_OTHER_USER(240),	// Desktop applications cannot set FBML for other users	
	UserPermission_API_EC_PERMISSION_STATUS_UPDATE(250),	 //Updating status requires the extended permission status_update.	 users.setStatus
	UserPermission_API_EC_PERMISSION_PHOTO_UPLOAD(260),	// Modifying existing photos requires the extended permission photo_upload	 photos.upload,photos.addTag
	UserPermission_API_EC_PERMISSION_VIDEO_UPLOAD(261),	// Modifying existing photos requires the extended permission photo_upload	 photos.upload,photos.addTag
	UserPermission_API_EC_PERMISSION_SMS(270),	 //Permissions disallow sms to user.	
	UserPermission_API_EC_PERMISSION_CREATE_LISTING(280),	 //Creating and modifying listings requires the extended permission create_listing	
	UserPermission_API_EC_PERMISSION_CREATE_NOTE(281),	// Managing notes requires the extended permission create_note.	
	UserPermission_API_EC_PERMISSION_SHARE_ITEM(282),	 //Managing shared items requires the extended permission share_item.	
	UserPermission_API_EC_PERMISSION_EVENT(290),	// Creating and modifying events requires the extended permission create_event	 events.create, events.edit
	UserPermission_API_EC_PERMISSION_LARGE_FBML_TEMPLATE(291),	 //FBML Template isn\'t owned by your application.	
	UserPermission_API_EC_PERMISSION_LIVEMESSAGE(292),	// An application is only allowed to send LiveMessages to users who have accepted the TOS for that application.	 liveMessage.send
	UserPermission_API_EC_PERMISSION_XMPP_LOGIN(293),	 //Logging in to chat requires the extended permission xmpp_login	 Integrating with FacebookChat
	UserPermission_API_EC_PERMISSION_ADS_MANAGEMENT(294),	// Managing advertisements requires the extended permission ads_management, and a participating API key	 Ads API
	UserPermission_API_EC_PERMISSION_CREATE_EVENT(296),	// Managing events requires the extended permission create_event	 API#Events_API_Methods
	UserPermission_API_EC_PERMISSION_READ_MAILBOX(298),	 //Reading mailbox messages requires the extended permission read_mailbox	 message.getThreadsInFolder
	UserPermission_API_EC_PERMISSION_RSVP_EVENT(299),	// RSVPing to events requires the extended permission create_rsvp
	
	
	CreditsFrontEnd_UnknownFacebookSystemIssue(1383001),
	CreditsFrontEnd_InvalidParameters(1383002),	//	Developer called with the incorrect parameters.
	CreditsFrontEnd_PaymentFailure(1383003),	//	Processor decline.
	CreditsFrontEnd_InvalidOperation(1383004),	//	Developer attempted an operation Facebook does not allow.
	CreditsFrontEnd_PermissionDenied(1383005),	//Facebook system issue.
	CreditsFrontEnd_DatabaseError(1383006),	//Facebook system issue.
	CreditsFrontEnd_InvalidApp(1383007),	//App is not whitelisted. Or while in test mode, Developer attempted to debit a user that was not whitelisted.
	CreditsFrontEnd_AppNoResponse(1383008),	//App is not responding; perhaps a server timeout issue.
	CreditsFrontEnd_AppErrorResponse(1383009),	//App responded to Facebook with an error code.
	CreditsFrontEnd_UserCanceled(1383010),	//User explicitly cancelled out of flow.
	CreditsFrontEnd_Disabled(1383011),	//Facebook system issue.
	CreditsFrontEnd_OrderFailureAfterPurchaseCredit(1383013),	//Facebook system issue.
	CreditsFrontEnd_DisputeFlow(1383014),	//Facebook system issue.
	CreditsFrontEnd_AccountNotCharged(1383015),	//Your application cancelled the order.
	CreditsFrontEnd_ExceedCreditBalanceLimit(1383017),	//Reached maximum number of credits the user is allowed to keep as a stored balance. This is a Facebook controlled limit and can vary from user to user.
	CreditsFrontEnd_ExceedCreditDailyPurchaseLimit(1383018),	//Occurs when a user has reached a predefined daily maximum
	CreditsFrontEnd_ExceedCreditDailySpendLimit(1383019),	//Occurs when the credit amount user spends in a single day exceeds a pre-defined threshold.
	CreditsFrontEnd_UserThrottled(1383040),	//Application Temporarily Unavailable
	CreditsFrontEnd_BuyerPaymentFailure(1383041),	//User's financial instrument could not be charged.
	CreditsFrontEnd_LoggedOutUser(1383042),	//Login Required
	CreditsFrontEnd_AppInfoFetchFailure(1383043),	//Facebook system error.
	CreditsFrontEnd_InvalidAppInfo(1383044),	//Application needs to have a valid callback url.
	CreditsFrontEnd_AppInvalidEncodedResponse(1383045),	//The application didn't return a valid json encoded response..
	CreditsFrontEnd_AppInvalidDecodedResponse(1383046),	//The application return value was invalid after json_decoding the return value.
	CreditsFrontEnd_AppInvalidMethodResponse(1383047),	//The application response contains a 'method' parameter that didn't match the request.
	CreditsFrontEnd_AppMissingContentResponse(1383048),	 //The application response didn't contain the 'content' field.
	CreditsFrontEnd_AppUnknownResponseError(1383049),	//The application returned an unknown response.
	CreditsFrontEnd_AppUserValidationFailedResponse(1383050),	//Failure to verify the user when sending application callback.
	CreditsFrontEnd_AppInvalidItemParam(1383051),	//The application is sending invalid item parameters (For example, price or quantity of the items is invalid).
	CreditsFrontEnd_EmptyAppId(1383052),	//Empty App ID.
	
	
	InfoSection_API_EC_INFO_NO_INFORMATION(1050),//	 No information has been set for this user	
	InfoSection_API_EC_INFO_SET_FAILED(1051);//	 Setting info failed. Check the formatting of your info fields
	private static Map<Integer, ErrorCode> codeMap = new HashMap<Integer, ErrorCode>();
	
	static {
		for (ErrorCode code : ErrorCode.values()) {
			codeMap.put(code.getCode(), code);
		}
	}
	public int getCode() {
		return errorCode;
	}
	public static ErrorCode parse(int code) {
		return codeMap.get(code);
	}
			
	ErrorCode(int code){
		this.errorCode = code;
	}
	private int errorCode;
}
