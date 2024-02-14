package com.ozan.familytask.model

data class Task(
    val taskId: String,
    val taskText: String,
    val memberId: String,
    val dueDate: String
)
