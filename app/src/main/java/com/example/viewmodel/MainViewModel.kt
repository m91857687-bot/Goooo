package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.data.MediaItem
import com.example.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class GalleryUiModel {
    data class Item(val mediaItem: MediaItem) : GalleryUiModel()
    data class Header(val title: String) : GalleryUiModel()
}

class MainViewModel(private val repository: MediaRepository) : ViewModel() {

    val mediaPagingData: Flow<PagingData<GalleryUiModel>> = Pager(
        config = PagingConfig(pageSize = 60, enablePlaceholders = true),
        pagingSourceFactory = { repository.getAllMedia() }
    ).flow
        .map { pagingData ->
            pagingData.map { GalleryUiModel.Item(it) }
        }
        .map { pagingData ->
            pagingData.insertSeparators { before: GalleryUiModel.Item?, after: GalleryUiModel.Item? ->
                if (before == null && after != null) {
                    GalleryUiModel.Header(formatDateHeader(after.mediaItem.dateAdded))
                } else if (before != null && after != null) {
                    val beforeDate = formatDateHeader(before.mediaItem.dateAdded)
                    val afterDate = formatDateHeader(after.mediaItem.dateAdded)
                    if (beforeDate != afterDate) {
                        GalleryUiModel.Header(afterDate)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
        .cachedIn(viewModelScope)

    fun sync() {
        viewModelScope.launch {
            repository.syncMedia()
        }
    }

    private fun formatDateHeader(dateInMillis: Long): String {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        
        calendar.timeInMillis = dateInMillis
        val itemYear = calendar.get(Calendar.YEAR)
        val itemDay = calendar.get(Calendar.DAY_OF_YEAR)

        return when {
            currentYear == itemYear && currentDay == itemDay -> "اليوم"
            currentYear == itemYear && currentDay - itemDay == 1 -> "أمس"
            currentYear == itemYear -> SimpleDateFormat("MMMM", Locale("ar")).format(Date(dateInMillis))
            else -> SimpleDateFormat("MMMM yyyy", Locale("ar")).format(Date(dateInMillis))
        }
    }
}

class MainViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
