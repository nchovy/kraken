package org.krakenapps.socialweb.facebook.graphapi.objectcode;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCode {
	/*
	 * Convention
	 * http://fbdevwiki.com/wiki/Error_codes
	 * "Errortype"_"Error Name"
	 * 	
	 * Error types are word sets consisted of initial upper case and rest lower cases without under bar
	 * 
	 * The Error name of "Front end error" is consisted of initial upper case and rest lower cases 
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
	
	DataEditing_API_EC_EDIT(300),	// Edit failure	
	DataEditing_API_EC_EDIT_USER_DATA(310),	 //User data edit failure	
	DataEditing_API_EC_EDIT_PHOTO(320),	// Photo edit failure	
	DataEditing_API_EC_EDIT_ALBUM_SIZE(321),	// Album is full	
	DataEditing_API_EC_EDIT_PHOTO_TAG_SUBJECT(322),	 //Invalid photo tag subject	
	DataEditing_API_EC_EDIT_PHOTO_TAG_PHOTO(323),	 //Cannot tag photo already visible on Facebook	
	DataEditing_API_EC_EDIT_PHOTO_FILE(324),	// Missing or invalid image file	
	DataEditing_API_EC_EDIT_PHOTO_PENDING_LIMIT(325),	 //Too many unapproved photos pending	
	DataEditing_API_EC_EDIT_PHOTO_TAG_LIMIT(326),	 //Too many photo tags pending	
	DataEditing_API_EC_EDIT_ALBUM_REORDER_PHOTO_NOT_IN_ALBUM(327),	 //Input array contains a photo not in the album	
	DataEditing_API_EC_EDIT_ALBUM_REORDER_TOO_FEW_PHOTOS(328),	// Input array has too few photos	
	DataEditing_API_EC_MALFORMED_MARKUP(329),	// Template data must be a JSON-encoded dictionary, of the form {'key-1': 'value-1', 'key-2': 'value-2', ...}	
	DataEditing_API_EC_EDIT_MARKUP(330),	// Failed to set markup	
	DataEditing_API_EC_EDIT_FEED_TOO_MANY_USER_CALLS(330),	 //Feed publication request limit reached	
	DataEditing_API_EC_EDIT_FEED_TOO_MANY_USER_ACTION_CALLS(341),	 //Feed action request limit reached	
	DataEditing_API_EC_EDIT_FEED_TITLE_LINK(342),	// Feed story title can have at most one href anchor	
	DataEditing_API_EC_EDIT_FEED_TITLE_LENGTH(343),	// Feed story title is too long	
	DataEditing_API_EC_EDIT_FEED_TITLE_NAME(344),	// Feed story title can have at most one fb:userlink and must be of the user whose action is being reported	
	DataEditing_API_EC_EDIT_FEED_TITLE_BLANK(345),	// Feed story title rendered as blank	
	DataEditing_API_EC_EDIT_FEED_BODY_LENGTH(346),	// Feed story body is too long	
	DataEditing_API_EC_EDIT_FEED_PHOTO_SRC(347),	// Feed story photo could not be accessed or proxied	
	DataEditing_API_EC_EDIT_FEED_PHOTO_LINK(348),	// Feed story photo link invalid	
	DataEditing_API_EC_EDIT_VIDEO_SIZE(350),	// Video file is too large	 video.upload
	DataEditing_API_EC_EDIT_VIDEO_INVALID_FILE(351),	 //Video file was corrupt or invalid	 
	DataEditing_API_EC_EDIT_VIDEO_INVALID_TYPE(352),	// Video file format is not supported	 
	DataEditing_API_EC_EDIT_VIDEO_FILE(353),	//Missing video file	 video.upload
	DataEditing_API_EC_EDIT_VIDEO_NOT_TAGGED(354),	// User is not tagged in this video	
	DataEditing_API_EC_EDIT_VIDEO_ALREADY_TAGGED(355),	// User is already tagged in this video	
	DataEditing_API_EC_EDIT_FEED_TITLE_ARRAY(360),	// Feed story title_data argument was not a valid JSON-encoded array	
	DataEditing_API_EC_EDIT_FEED_TITLE_PARAMS(361),	// Feed story title template either missing required parameters, or did not have all parameters defined in title_data array	
	DataEditing_API_EC_EDIT_FEED_BODY_ARRAY(362),	// Feed story body_data argument was not a valid JSON-encoded array	
	DataEditing_API_EC_EDIT_FEED_BODY_PARAMS(363),	// Feed story body template either missing required parameters, or did not have all parameters defined in body_data array	
	DataEditing_API_EC_EDIT_FEED_PHOTO(364),	 //Feed story photos could not be retrieved, or bad image links were provided	
	DataEditing_API_EC_EDIT_FEED_TEMPLATE(365),	 //The template for this story does not match any templates registered for this application	
	DataEditing_API_EC_EDIT_FEED_TARGET(366),	 //One or more of the target ids for this story are invalid. They must all be ids of friends of the acting user	
	DataEditing_API_EC_EDIT_FEED_MARKUP(367),	 //The template data provided doesn't cover the entire token set needed to publish the story	
	DataEditing_Warning(368),// This Message Contains Blocked Content: Some content in this message has been reported as abusive by Facebook users.	
	DataEditing_API_EC_USERS_CREATE_INVALID_EMAIL(370),	 //The email address you provided is not a valid email address	
	DataEditing_API_EC_USERS_CREATE_EXISTING_EMAIL(371),	 //The email address you provided belongs to an existing account	
	DataEditing_API_EC_USERS_CREATE_BIRTHDAY(372),	 //The birthday provided is not valid	
	DataEditing_API_EC_USERS_CREATE_PASSWORD(373),	 //The password provided is too short or weak	
	DataEditing_API_EC_USERS_REGISTER_INVALID_CREDENTIAL(374),	 //The login credential you provided is invalid.	
	DataEditing_API_EC_USERS_REGISTER_CONF_FAILURE(375),	 //Failed to send confirmation message to the specified login credential.	
	DataEditing_API_EC_USERS_REGISTER_EXISTING(376),	 //The login credential you provided belongs to an existing account	
	DataEditing_API_EC_USERS_REGISTER_DEFAULT_ERROR(377),	 //Sorry, we were unable to process your registration.	
	DataEditing_API_EC_USERS_REGISTER_PASSWORD_BLANK(378),	// Your password cannot be blank. Please try another.	
	DataEditing_API_EC_USERS_REGISTER_PASSWORD_INVALID_CHARS(379),	// Your password contains invalid characters. Please try another.	
	DataEditing_API_EC_USERS_REGISTER_PASSWORD_SHORT(380),	// Your password must be at least 6 characters long. Please try another.	
	DataEditing_API_EC_USERS_REGISTER_PASSWORD_WEAK(381),	// Your password should be more secure. Please try another.	
	DataEditing_API_EC_USERS_REGISTER_USERNAME_ERROR(382),	// Our automated system will not approve this name.	
	DataEditing_API_EC_USERS_REGISTER_MISSING_INPUT(383),	// You must fill in all of the fields.	
	DataEditing_API_EC_USERS_REGISTER_INCOMPLETE_BDAY(384),	 //You must indicate your full birthday to register.	
	DataEditing_API_EC_USERS_REGISTER_INVALID_EMAIL(385),	// Please enter a valid email address.	
	DataEditing_API_EC_USERS_REGISTER_EMAIL_DISABLED(386),	// The email address you entered has been disabled. Please contact disabled@facebook.com with any questions.	
	DataEditing_API_EC_USERS_REGISTER_ADD_USER_FAILED(387),	// There was an error with your registration. Please try registering again.	
	DataEditing_API_EC_USERS_REGISTER_NO_GENDER(388),	 //Please select either Male or Female.
	
	Authentication_API_EC_AUTH_EMAIL(400),	 //Invalid email address	
	Authentication_API_EC_AUTH_LOGIN(401),	// Invalid username or password	
	Authentication_API_EC_AUTH_SIG(402),	// Invalid application auth sig	
	Authentication_API_EC_AUTH_TIME(403),	// Invalid timestamp for authentication	
	
	Session_API_EC_SESSION_TIMED_OUT(450),	 //Session key specified has passed its expiration time	
	Session_API_EC_SESSION_METHOD(451),	 //Session key specified cannot be used to call this method	
	Session_API_EC_SESSION_INVALID(452),	 //Session key invalid. This could be because the session key has an incorrect format, or because the user has revoked this session	
	Session_API_EC_SESSION_REQUIRED(453),	// A session key is required for calling this method	
	Session_API_EC_SESSION_REQUIRED_FOR_SECRET(454),	// A session key must be specified when request is signed with a session secret	
	Session_API_EC_SESSION_CANNOT_USE_SESSION_SECRET(455),	// A session secret is not permitted to be used with this type of session key
	
	ApplicationMessaging_API_EC_MESG_BANNED(500),//	 Message contains banned content	
	ApplicationMessaging_API_EC_MESG_NO_BODY(501),//	 Missing message body	
	ApplicationMessaging_API_EC_MESG_TOO_LONG(502),	 //Message is too long	
	ApplicationMessaging_API_EC_MESG_RATE(503),//	 User has sent too many messages	
	ApplicationMessaging_API_EC_MESG_INVALID_THREAD(504),//	 Invalid reply thread id	
	ApplicationMessaging_API_EC_MESG_INVALID_RECIP(505),//	 Invalid message recipient	
	ApplicationMessaging_API_EC_POKE_INVALID_RECIP(510),	// Invalid poke recipient	
	ApplicationMessaging_API_EC_POKE_OUTSTANDING(511),//	 There is a poke already outstanding	
	ApplicationMessaging_API_EC_POKE_RATE(512),//	 User is poking too fast	
	ApplicationMessaging_API_EC_POKE_USER_BLOCKED(513),//	 User cannot poke via API
	
	FQL_EC_UNKNOWN_ERROR(600),//	 An unknown error occurred in FQL	 fql.query,fql.multiquery
	FQL_EC_PARSER_ERROR(601),//	 Error while parsing FQL statement	 fql.query,fql.multiquery
	FQL_EC_UNKNOWN_FIELD(602),//	 The field you requested does not exist	 fql.query,fql.multiquery
	FQL_EC_UNKNOWN_TABLE(603),//	 The table you requested does not exist	 fql.query,fql.multiquery
	FQL_EC_NO_INDEX(604),//	 Your statement is not indexable	 fql.query,fql.multiquery
	FQL_EC_UNKNOWN_FUNCTION(605),//	 The function you called does not exist	 fql.query,fql.multiquery
	FQL_EC_INVALID_PARAM(606),//	 Wrong number of arguments passed into the function	 fql.query,fql.multiquery
	FQL_EC_INVALID_FIELD(607),//	 FQL field specified is invalid in this context.	 fql.query*,fql.multiquery
	FQL_EC_INVALID_SESSION(608),//	 An invalid session was specified	 fql.query,fql.multiquery
	FQL_EC_UNSUPPORTED_APP_TYPE(609),//	 FQL field specified is invalid in this context.	 fql.query*,fql.multiquery
	FQL_EC_SESSION_SECRET_NOT_ALLOWED(610),//	 FQL field specified is invalid in this context.	 fql.query*,fql.multiquery
	FQL_EC_DEPRECATED_TABLE(611),//	 FQL field specified is invalid in this context.	 fql.query*,fql.multiquery
	FQL_EC_EXTENDED_PERMISSION(612),//	 The stream requires an extended permission	 fql.query,fql.multiquery
	FQL_EC_RATE_LIMIT_EXCEEDED(613),//	 Calls to stream have exceeded the rate of 100 calls per 600 seconds.	 fql.query,fql.multiquery
	FQL_EC_UNRESOLVED_DEPENDENCY(614),//	 Unresolved dependency in multiquery	 fql.multiquery
	FQL_EC_INVALID_SEARCH(615),//	 This search is invalid	 fql.query,fql.multiquery
	FQL_EC_TOO_MANY_FRIENDS_FOR_PRELOAD(617),//	 The user you queried against has too many friends to be used with Preload FQL, in order to avoid out of memory errors	 fql.query,fql.multiquery
	//FQL: This error is returned when the field name is sometimes valid, but not all the time. For example, if you run fql.query on the Metrics FQL table, you can get this error because some metrics are queryable only over the daily period, as opposed to the weekly or monthly periods.
	
	Ref_API_EC_REF_SET_FAILED(700),//	 Unknown failure in storing ref data. Please try again.
	
	ApplicationIntegration_API_EC_FB_APP_UNKNOWN_ERROR(750),//	 Unknown Facebook application integration failure.	
	ApplicationIntegration_API_EC_FB_APP_FETCH_FAILED(751),//	 Fetch from remote site failed.	
	ApplicationIntegration_API_EC_FB_APP_NO_DATA(752),//	 Application returned no data. This may be expected or represent a connectivity error.	
	ApplicationIntegration_API_EC_FB_APP_NO_PERMISSIONS(753),//	 Application returned user had invalid permissions to complete the operation.	
	ApplicationIntegration_API_EC_FB_APP_TAG_MISSING(754),//	 Application returned data, but no matching tag found. This may be expected.	
	ApplicationIntegration_API_EC_FB_APP_DB_FAILURE(755),//	 The database for this object failed.
	
	ApplicationInformation_API_EC_NO_SUCH_APP(900),//	 No such application exists.
	
	BatchAPI_API_BATCH_TOO_MANY_ITEMS(950),//	 Each batch API can not contain more than 20 items	
	BatchAPI_API_EC_BATCH_ALREADY_STARTED(951),//	 begin_batch already called, please make sure to call end_batch first.	
	BatchAPI_API_EC_BATCH_NOT_STARTED(952),//	 end_batch called before begin_batch.	
	BatchAPI_API_EC_BATCH_METHOD_NOT_ALLOWED_IN_BATCH_MODE(953),//	 This method is not allowed in batch mode.
	
	EventAPI_API_EC_EVENT_INVALID_TIME(1000),//	 Invalid time for an event.	 events.edit
	EventAPI_API_EC_EVENT_NAME_LOCKED(1001),//	 You are no longer able to change the name of this event.
	
	LiveMessage_API_EC_LIVEMESSAGE_SEND_FAILED(1100),//	 An error occurred while sending the LiveMessage.	liveMessage.send
	LiveMessage_API_EC_LIVEMESSAGE_EVENT_NAME_TOO_LONG(1101),//	 The event_name parameter must be no longer than 128 bytes.	liveMessage.send
	LiveMessage_API_EC_LIVEMESSAGE_MESSAGE_TOO_LONG(1102),//	 The message parameter must be no longer than 1024 bytes
	
	Chat_API_EC_CHAT_SEND_FAILED(1200),//	 An error occurred while sending the message
	
	FacebookPage_API_EC_PAGES_CREATE(1201),//	 You have created too many pages	

	FacebookLinks_API_EC_SHARE_BAD_URL(1500),//	 The url you supplied is invalid	
	
	FacebookNotes_API_EC_NOTE_CANNOT_MODIFY(1600),//	 The user does not have permission to modify this note.
	
	Comment_API_EC_COMMENTS_UNKNOWN(1700),//	 An unknown error has occurred.	
	Comment_API_EC_COMMENTS_POST_TOO_LONG(1701),//	 The specified post was too long.	
	Comment_API_EC_COMMENTS_DB_DOWN(1702),//	 The comments database is down.	
	Comment_API_EC_COMMENTS_INVALID_XID(1703),//	 The specified xid is not valid. xids can only contain letters, numbers, and underscores	
	Comment_API_EC_COMMENTS_INVALID_UID(1704),//	 The specified user is not a user of this application	
	Comment_API_EC_COMMENTS_INVALID_POST(1705),//	 There was an error during posting.	
	Comment_API_EC_COMMENTS_INVALID_REMOVE(1706),//	 While attempting to remove the post.
	
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
	
	//Back End Error Codes
	BackEnd_API_EC_PAYMENTS_UNKNOWN(1150),	 //	 Unknown error	
	BackEnd_API_EC_PAYMENTS_APP_INVALID(1151),	 //Application is not enabled for using Facebook Credits.	
	BackEnd_API_EC_PAYMENTS_DATABASE(1152),	// A database error occurred.	
	BackEnd_API_EC_PAYMENTS_PERMISSION_DENIED(1153),	// Permission denied to check order details.	
	BackEnd_API_EC_PAYMENTS_APP_NO_RESPONSE(1154),	 //Payments callback to the application failed.	
	BackEnd_API_EC_PAYMENTS_APP_ERROR_RESPONSE(1155),//	 Payments callback to the application received error response.	
	BackEnd_API_EC_PAYMENTS_INVALID_ORDER(1156),	// The supplied order ID is invalid.	
	BackEnd_API_EC_PAYMENTS_INVALID_PARAM(1157),//	 One of the Payments parameters is invalid.	
	BackEnd_API_EC_PAYMENTS_INVALID_OPERATION(1158),//	 The operation is invalid.	
	BackEnd_API_EC_PAYMENTS_PAYMENT_FAILED(1159),//	 Failed in processing the payment.	
	BackEnd_API_EC_PAYMENTS_DISABLED(1160),//	 Facebook Credits system is disabled.	
	BackEnd_API_EC_PAYMENTS_INSUFFICIENT_BALANCE(1161),//	 Insufficient balance.	
	BackEnd_API_EC_PAYMENTS_EXCEED_CREDIT_BALANCE_LIMIT(1162),//	 Exceed credit balance limit.	
	BackEnd_API_EC_PAYMENTS_EXCEED_CREDIT_DAILY_PURCHASE_LIMIT(1163),//	 Exceed daily credit purchase limit.	
	BackEnd_API_EC_PAYMENTS_EXCEED_CREDIT_DAILY_SPEND_LIMIT(1164),//	 Exceed daily credit spend limit.	
	BackEnd_API_EC_PAYMENTS_INVALID_FUNDING_AMOUNT(1166),//	 Credits purchased from funding source do not match the spend order amount.	
	BackEnd_API_EC_PAYMENTS_NON_REFUNDABLE_PAYMENT_METHOD(1167),//	 The funding source is a non-refundable payment method.	
	BackEnd_API_EC_PAYMENTS_USER_THROTTLED(1168),//	 Application is configured to throttle some users.	
	BackEnd_API_EC_PAYMENTS_LOGIN_REQUIRED(1169),//	 User is not logged in.	
	BackEnd_API_EC_APP_INFO_FETCH_FAILURE(1170),//	 Error retrieving application information.	
	BackEnd_API_EC_INVALID_APP_INFO(1171),//	 Invalid application information returned.	
	BackEnd_API_EC_PAYMENTS_APP_INSUFFICIENT_BALANCE(1172),//	 Application has insufficient balance (app2user).
	
//	Data Store API Errors
	DataStore_API_EC_DATA_UNKNOWN_ERROR(800),//	 Unknown data store API error	
	DataStore_API_EC_DATA_INVALID_OPERATION(801),//	 Invalid operation	
	DataStore_API_EC_DATA_QUOTA_EXCEEDED(802),//	 Data store allowable quota was exceeded	
	DataStore_API_EC_DATA_OBJECT_NOT_FOUND(803),//	 Specified object cannot be found	
	DataStore_API_EC_DATA_OBJECT_ALREADY_EXISTS(804),//	 Specified object already exists	
	DataStore_API_EC_DATA_DATABASE_ERROR(805),//	 A database error occurred. Please try again	
	DataStore_API_EC_DATA_CREATE_TEMPLATE_ERROR(806),//	 Unable to add FBML template to template database. Please try again.	
	DataStore_API_EC_DATA_TEMPLATE_EXISTS_ERROR(807),//	 No active template bundle with that ID or handle exists.	
	DataStore_API_EC_DATA_TEMPLATE_HANDLE_TOO_LONG(808),//	 Template bundle handles must contain less than or equal to 32 characters.	
	DataStore_API_EC_DATA_TEMPLATE_HANDLE_ALREADY_IN_USE(809),//	 Template bundle handle already identifies a previously registered template bundle, and handles can not be reused.	
	DataStore_API_EC_DATA_TOO_MANY_TEMPLATE_BUNDLES(810),//	 Application has too many active template bundles, and some must be deactivated before new ones can be registered.	
	DataStore_API_EC_DATA_MALFORMED_ACTION_LINK(811),//	 One of more of the supplied action links was improperly formatted.	
	DataStore_API_EC_DATA_TEMPLATE_USES_RESERVED_TOKEN(812),//	 One …or more of your templates is using a token reserved by Facebook, such as {*mp3*} or {*video*}.
	
	//Mobile/SMS Errors
	Mobile_API_EC_SMS_INVALID_SESSION(850),//	 Invalid sms session.	
	Mobile_API_EC_SMS_MSG_LEN(851),//	 Invalid sms message length.	
	Mobile_API_EC_SMS_USER_QUOTA(852),//	 Over user daily sms quota.	
	Mobile_API_EC_SMS_USER_ASLEEP(853),//	 Unable to send sms to user at this time.	
	Mobile_API_EC_SMS_APP_QUOTA(854),//	 Over application daily sms quota/rate limit.	
	Mobile_API_EC_SMS_NOT_REGISTERED(855),//	 User is not registered for Facebook Mobile Texts	
	Mobile_API_EC_SMS_NOTIFICATIONS_OFF(856),//	 User has SMS notifications turned off	
	Mobile_API_EC_SMS_CARRIER_DISABLE(857),//	 SMS application disallowed by mobile operator
	
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
