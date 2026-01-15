package com.example.monoplayer

sealed class Screen (val route:String){
    object MainScreen:Screen("main_screen")
}