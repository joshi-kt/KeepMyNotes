package com.example.keepmynotes.utils

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Dangerous API please use consciously"
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION
)
annotation class RestrictedAPI