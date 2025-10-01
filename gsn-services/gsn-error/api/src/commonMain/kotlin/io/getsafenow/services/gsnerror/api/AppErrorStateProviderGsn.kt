package io.getsafenow.services.gsnerror.api

fun aAppErrorState() = AppErrorStateGsn.Error(
    title = "Error occurred",
    body = "Something went wrong in the gsn app, and the details of that would go here.",
    dismiss = {},
)