//package com.example.easybot
//
//object Routes {
//    var Registrationpage = "Registration_page"
//    var ChatPage = "Chat_Page"
//    const val SignIn = "sign_in"
//    const val SignUp = "sign_up"
//    const val ChatWithArg = "chat/{username}"
//
//}

package com.example.easybot

object Routes {
    const val ChatList = "chat_list"             // экран со списком чатов
    const val ChatPage = "chat"                  // базовый путь
    const val ChatPageWithArg = "chat/{chatId}"  // маршрут с аргументом chatId

    const val SignIn = "sign_in"
    const val SignUp = "sign_up"
    const val RegistrationPage = "registration_page"
}
