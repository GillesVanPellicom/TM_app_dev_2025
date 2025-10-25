package com.example.movietracker.repository

/**
 * Thrown when a network request fails and no cached data is available for the
 * requested resource. The UI layer should catch this to trigger the error
 * state (reload button + dialog).
 */
class OfflineNoCacheException(message: String) : Exception(message)
