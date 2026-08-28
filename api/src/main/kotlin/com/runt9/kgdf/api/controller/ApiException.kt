package com.runt9.kgdf.api.controller

import io.ktor.http.HttpStatusCode

class ApiException(override val message: String?, val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError) : Exception()
