package com.mobdeve.s15.group8.mobdeve_mp.controller.interfaces

interface ImageUploadCallback {
    fun onCloudinaryUploadSuccess(imageUrl: String)
}