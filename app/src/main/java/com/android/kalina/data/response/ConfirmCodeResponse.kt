package com.android.kalina.data.response

import com.google.gson.annotations.SerializedName

class ConfirmCodeResponse {
    var status: Int = ApiResponse.STATUS_CANCEL
    var message: String = ""
    @SerializedName("device_code")
    var deviceCode: String = ""
}
