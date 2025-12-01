package com.duolingo.clone.language_backend.dto

import com.duolingo.clone.language_backend.entity.TransactionType

data class PurchaseRequest(
    val itemType: TransactionType
)