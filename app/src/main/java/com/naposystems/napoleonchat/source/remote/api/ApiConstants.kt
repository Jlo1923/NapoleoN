package com.naposystems.napoleonchat.source.remote.api

object ApiConstants {

    const val GENERATE_CODE = "auth/sendverificationcode"
    const val VERIFICATE_CODE = "auth/validateverificationcode"
    const val VALIDATE_NICKNAME = "auth/validatenick"
    const val CREATE_ACCOUNT = "users"
    const val UPDATE_USER_INFO = "users/update"
    const val UPDATE_MUTE_CONVERSATION = "friendship/silence/{id}"
    const val SEND_PQRS = "pqrs"
    const val FRIEND_SHIP_SEARCH = "friendship/search/{state}"
    const val FRIEND_SHIP_SEARCH_BY_DATE = "friendship/search/{state}"
    const val SEND_MESSAGE = "messages"
    const val SEND_MESSAGE_ATTACHMENT = "messages/attachment"
    const val SEND_MESSAGE_TEST = "storemessagetest"
    const val GET_MY_MESSAGES = "messages/getmymessages"
    const val VERIFY_MESSAGES_RECEIVED = "messages/verifymessagesreceived"
    const val VERIFY_MESSAGES_READ = "messages/verifymessagesreaded"
    const val SEND_MESSAGES_READ = "messages/sendmessagesreaded"
    const val NOTIFY_MESSAGE_RECEIVED = "messages/notifymessagereceived"
    const val GET_QUESTIONS = "questions"
    const val SEND_QUESTIONS = "inforecovery"
    const val GET_RECOVERY_QUESTIONS = "inforecovery/getanswersinforecovery/{nick}"
    const val SEND_ANSWERS = "inforecovery/validateanswers"
    const val SEARCH_USER = "users/search/{nick}"
    const val SEND_FRIENDSHIP_REQUEST = "friendshiprequest"
    const val GET_FRIENDSHIP_REQUESTS = "friendshiprequest"
    const val GET_FRIENDSHIP_REQUESTS_RECEIVED = "friendshiprequest/friendshipRequestReceived"
    const val PUT_FRIENDSHIP_REQUEST = "friendshiprequest/{id}"
    const val GET_FRIENDSHIP_REQUEST_QUANTITY = "friendshiprequest/countfriendshiprequest"
    const val PUT_BLOCK_CONTACT = "friendship/blockuser/{id}"
    const val DELETE_CONTACT = "friendship/{id}"
    const val DELETE_MESSAGES_FOR_ALL = "destroymessages"
    const val PUT_UNBLOCK_CONTACT = "friendship/unblockuser/{id}"
    const val VALIDATE_PASSWORD_OLD_ACCOUNT = "inforecovery/validateoldpassword"
    const val GET_QUESTIONS_OLD_USER = "inforecovery/getanswersinforecoveryolduser/{nick}"
    const val VALIDATE_ANSWERS_OLD_USER = "inforecovery/validateanswersolduser"
    const val BLOCK_ATTACKER = "inforecovery/blockattacker"
    const val GET_SUBSCRIPTION_USER = "payments/ultimatepayment"
    const val TYPE_SUBSCRIPTIONS = "subscriptions"
    const val SEND_SELECTED_SUBSCRIPTION = "paypal/createpayment"
    const val CALL_CONTACT = "call/callfriend"
    const val REJECT_CALL = "call/rejectedcall"
    const val LOG_OUT = "auth/logout"
    const val CANCEL_SUBSCRIPTION = "subscriptions/cancel"
    const val CHECK_SUBSCRIPTION = "subscriptions/state"
    const val LAST_SUBSCRIPTION = "lastsubscription"
    const val CANCEL_CALL = "call/cancelcall"
    const val READY_CALL = "call/readyforcall"
    const val UPDATE_CONTACT_FAKE = "friendship/updatefriendship/{friendshipId}"
    const val UPDATE_SUSCRIPTION = "friendship/updatefriendship/{friendshipId}"

}