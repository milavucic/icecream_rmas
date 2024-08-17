package com.example.icecream.repositories

import com.example.icecream.data.Icecream
import com.example.icecream.data.Mark

interface MarkRepository {
    suspend fun addIcecreamMark(
        icecreamid: String,
        icecream: Icecream,
        mark: Int
    ): Resource<String>
    suspend fun getIcecreamMarks(
        icecreamid: String
    ): Resource<List<Mark>>
    suspend fun getUserMarks(): Resource<List<Mark>>

    suspend fun updateIcecreamMark(
        markid: String,
        mark: Int,
    ): Resource<String>
}