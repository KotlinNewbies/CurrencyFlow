//package com.example.currencyflow
//
//import java.util.UUID
//
//object UUIDMenadzer{
//    private var przechowywanyUUID: String? = null
//
//    fun zdobadzUUID(): String{
//        if (przechowywanyUUID == null) {
//            przechowywanyUUID = generatorUUID()
//        }
//        return przechowywanyUUID!!
//    }
//    private fun generatorUUID(): String {
//        val mojeUUID = UUID.randomUUID()
//        return mojeUUID.toString()
//    }
//}