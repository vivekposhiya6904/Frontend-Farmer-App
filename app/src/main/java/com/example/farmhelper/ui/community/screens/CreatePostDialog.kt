package com.example.farmhelper.ui.community.screens

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.farmhelper.ui.community.viewmodel.CreatePostUiState
import com.example.farmhelper.ui.theme.*
import com.example.farmhelper.ui.weather.location.AppLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

enum class MediaType { NONE, IMAGE, VIDEO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    createPostState: CreatePostUiState,
    onDismiss: () -> Unit,
    onSubmit: (content: String, cropTag: String, location: String?, imageFile: File?, videoFile: File?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var contentText by remember { mutableStateOf("") }
    var selectedCropTag by remember { mutableStateOf("General") }
    var locationText by remember { mutableStateOf("") }
    var isFetchingLocation by remember { mutableStateOf(false) }

    var mediaType by remember { mutableStateOf(MediaType.NONE) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraVideoUri by remember { mutableStateOf<Uri?>(null) }

    val isSubmitting = createPostState is CreatePostUiState.Submitting
    val submittingStatusText = if (createPostState is CreatePostUiState.Submitting) createPostState.statusText else "Publishing post..."

    val cropTags = listOf("General", "Cotton", "Wheat", "Groundnut", "Rice", "Vegetables", "Sugarcane", "Bajra")

    // Helper: Create temp file in cache for FileProvider media capture
    fun createTempMediaFile(extension: String): File {
        val cacheDir = context.cacheDir
        return File.createTempFile("captured_media_${System.currentTimeMillis()}", extension, cacheDir)
    }

    // Helper: FileProvider Uri from file
    fun getFileProviderUri(file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Camera Photo Launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraPhotoUri != null) {
            selectedMediaUri = cameraPhotoUri
            mediaType = MediaType.IMAGE
        }
    }

    // Camera Video Launcher
    val takeVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && cameraVideoUri != null) {
            selectedMediaUri = cameraVideoUri
            mediaType = MediaType.VIDEO
        }
    }

    // Gallery Image Picker Launcher
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            mediaType = MediaType.IMAGE
        }
    }

    // Gallery Video Picker Launcher
    val pickVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            mediaType = MediaType.VIDEO
        }
    }

    // Permission Launcher for Camera Photo
    val cameraPhotoPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            try {
                val tempFile = createTempMediaFile(".jpg")
                val uri = getFileProviderUri(tempFile)
                cameraPhotoUri = uri
                takePhotoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error launching camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launcher for Camera Video
    val cameraVideoPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            try {
                val tempFile = createTempMediaFile(".mp4")
                val uri = getFileProviderUri(tempFile)
                cameraVideoUri = uri
                takeVideoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Error launching video camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to record video", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launcher for GPS Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            isFetchingLocation = true
            coroutineScope.launch {
                val loc = AppLocationProvider.getCurrentLocation(context)
                if (loc != null) {
                    val resolved = withContext(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                                "$city, Gujarat"
                            } else "Rajkot, Gujarat"
                        } catch (e: Exception) {
                            "Rajkot, Gujarat"
                        }
                    }
                    locationText = resolved
                } else {
                    locationText = "Rajkot, Gujarat"
                }
                isFetchingLocation = false
            }
        } else {
            locationText = "Gujarat, India"
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = WarmWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Community Post",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 18.sp
                    )
                )
                IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MediumGrayText
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                // Post Content Input
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    placeholder = { Text("Share farm update, question, or disease advice with farmers...", color = MediumGrayText, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = SoftOlive,
                        focusedContainerColor = WarmWhite,
                        unfocusedContainerColor = SoftOlive.copy(alpha = 0.3f)
                    ),
                    maxLines = 5,
                    enabled = !isSubmitting
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Crop Category Chips
                Text(
                    text = "Crop Tag",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkGrayText,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cropTags.forEach { tag ->
                        val isSelected = tag == selectedCropTag
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCropTag = tag },
                            label = { Text(tag, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = White,
                                containerColor = SoftOlive,
                                labelColor = DarkGrayText
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isSubmitting
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location Picker Row with GPS Auto Fetch
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    placeholder = { Text("Village / District (e.g. Rajkot)", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            enabled = !isSubmitting
                        ) {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ForestGreen, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Outlined.MyLocation, contentDescription = "Auto GPS", tint = ForestGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = SoftOlive
                    ),
                    enabled = !isSubmitting
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Media Preview Card
                if (mediaType != MediaType.NONE && selectedMediaUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftOlive)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (mediaType == MediaType.IMAGE) {
                                AsyncImage(
                                    model = selectedMediaUri,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Outlined.Videocam, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Video Attached", style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontWeight = FontWeight.Bold))
                                }
                            }

                            // Remove Media Button (Top Right)
                            IconButton(
                                onClick = {
                                    selectedMediaUri = null
                                    mediaType = MediaType.NONE
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(WarmWhite.copy(alpha = 0.9f))
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = AlertRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Media Action Buttons Row (Camera Photo, Camera Video, Gallery Image, Gallery Video)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Photo
                    IconButton(
                        onClick = {
                            cameraPhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        enabled = !isSubmitting
                    ) {
                        Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = "Take Photo", tint = ForestGreen)
                    }

                    // Camera Video
                    IconButton(
                        onClick = {
                            cameraVideoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        enabled = !isSubmitting
                    ) {
                        Icon(imageVector = Icons.Outlined.Videocam, contentDescription = "Record Video", tint = ForestGreen)
                    }

                    // Gallery Image
                    IconButton(
                        onClick = {
                            pickImageLauncher.launch("image/*")
                        },
                        enabled = !isSubmitting
                    ) {
                        Icon(imageVector = Icons.Outlined.Image, contentDescription = "Pick Image", tint = ForestGreen)
                    }

                    // Gallery Video
                    IconButton(
                        onClick = {
                            pickVideoLauncher.launch("video/*")
                        },
                        enabled = !isSubmitting
                    ) {
                        Icon(imageVector = Icons.Outlined.Videocam, contentDescription = "Pick Video", tint = ForestGreen)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (contentText.trim().isNotBlank()) {
                        var imgFile: File? = null
                        var vidFile: File? = null

                        val activeUri = selectedMediaUri
                        if (activeUri != null) {
                            if (mediaType == MediaType.IMAGE) {
                                imgFile = copyUriToCacheFile(context, activeUri, ".jpg")
                            } else if (mediaType == MediaType.VIDEO) {
                                vidFile = copyUriToCacheFile(context, activeUri, ".mp4")
                            }
                        }

                        val loc = if (locationText.trim().isNotBlank()) locationText.trim() else null
                        onSubmit(contentText.trim(), selectedCropTag, loc, imgFile, vidFile)
                    }
                },
                enabled = contentText.trim().length >= 3 && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = White,
                    disabledContainerColor = ForestGreen.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(submittingStatusText, fontSize = 12.sp)
                } else {
                    Text("Publish Post", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel", color = MediumGrayText)
            }
        }
    )
}

// Utility: Copy Content Uri stream into temporary File for Retrofit multipart upload
private fun copyUriToCacheFile(context: Context, uri: Uri, suffix: String): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_temp_${System.currentTimeMillis()}", suffix, context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        null
    }
}
