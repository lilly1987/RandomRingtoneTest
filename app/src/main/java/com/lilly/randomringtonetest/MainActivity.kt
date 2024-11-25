package com.lilly.randomringtonetest

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.io.File
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.lilly.randomringtonetest.ui.theme.RandomRingtoneTestTheme

class MainActivity : ComponentActivity() {
    //var list: MutableList<Pair<String, Boolean>> = mutableListOf()
    private var map= mutableMapOf<String, MutableState<Boolean>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //val context = LocalContext.current
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
                //,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,android.Manifest.permission.READ_MEDIA_AUDIO
            )
            ,Context.MODE_PRIVATE
        )

        val directory :File= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
        map[directory.absolutePath]= mutableStateOf( true)

        getFilesApi29Plus(this,directory.absolutePath).forEach {
            map[it]= mutableStateOf( true)
        }

        //val folderManager = FolderMusicManager()
        //// 하위 폴더 포함하여 모든 음악 가져오기
        //val allMusicInSubFolders = folderManager.getMusicFromSubFolders(
        //    this,
        //    "/storage/emulated/0"
        //    //"/storage/emulated/0/Ringtones"
        //)
//
        //map["${allMusicInSubFolders.size}"]= mutableStateOf( true)
//
        //allMusicInSubFolders.forEach {
        //    map[it.path]= mutableStateOf( true)
        //}

        //map[DIRECTORY_RINGTONES]= mutableStateOf( false)
        //if (directory.exists() && directory.isDirectory) {
        //    map[directory.path]= mutableStateOf( true)
        //    map[directory.absolutePath]= mutableStateOf( true)
        //    //getMusicFromFolder(this,directory.path).forEach {
        //    //    map[it.path]= mutableStateOf( true)
        //    //}
        //    //val list=getMusicFromSubFolders(this,directory.path)
        //    //val list=getMusicFromSubFolders(this,"/")
        //    val list=getMusicFromSubFolders(this,"/")
        //    map["${list.size}"]= mutableStateOf( true)
        //    list.forEach {
        //        map[it.path]= mutableStateOf( true)
        //    }
        //}else{
        //    Toast.makeText(this, "$directory.path 존재 안함", Toast.LENGTH_SHORT).show()
        //}

        //for (i:Int in 1..99){
        //    //list.add("file $i" to false)
        //    map["file $i 1342"]= mutableStateOf( true)
        //}

        setContent {
            RandomRingtoneTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        map,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun getFilesApi29Plus(context: Context, folderPath: String): List<String> {
    val fileList = mutableListOf<String>()

    val projection = arrayOf(
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.MediaColumns.DISPLAY_NAME
    )

    val selectionArgs = arrayOf("$folderPath%")

    context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        selectionArgs,
        null
    )?.use { cursor ->
        val pathColumn =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
        val nameColumn =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

        while (cursor.moveToNext()) {
            val path = cursor.getString(pathColumn)
            val name = cursor.getString(nameColumn)
            fileList.add("$path$name")
        }
    }

    return fileList
}

class FolderMusicManager {
    data class MusicFile(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val path: String,
        val size: Long,
        val folderPath: String,
        val folderName: String
    )

    // 특정 폴더의 음악 파일 목록 가져오기
    fun getMusicFromFolder(context: Context, folderPath: String): List<MusicFile> {
        val musicList = mutableListOf<MusicFile>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        )

        // 특정 폴더 내의 음악 파일만 필터링
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$folderPath%")

        // 제목 기준으로 정렬
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

                val file = File(path)
                val folder = file.parentFile

                musicList.add(
                    MusicFile(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        path = path,
                        size = size,
                        folderPath = folder?.absolutePath ?: "",
                        folderName = folder?.name ?: ""
                    )
                )
            }
        }
        return musicList
    }

    // 음악이 포함된 모든 폴더 목록 가져오기
    fun getMusicFolders(context: Context): List<FolderInfo> {
        val folders = mutableMapOf<String, FolderInfo>()

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val file = File(path)
                val folder = file.parentFile

                if (folder != null) {
                    val folderPath = folder.absolutePath
                    if (!folders.containsKey(folderPath)) {
                        folders[folderPath] = FolderInfo(
                            path = folderPath,
                            name = folder.name,
                            musicCount = 1
                        )
                    } else {
                        folders[folderPath]?.let {
                            folders[folderPath] = it.copy(musicCount = it.musicCount + 1)
                        }
                    }
                }
            }
        }
        return folders.values.toList()
    }

    // 특정 확장자의 음악 파일만 가져오기 (예: mp3, flac 등)
    fun getMusicByExtension(context: Context, folderPath: String, extensions: List<String>): List<MusicFile> {
        val musicList = getMusicFromFolder(context, folderPath)
        return musicList.filter { music ->
            extensions.any { ext ->
                music.path.lowercase().endsWith(".$ext")
            }
        }
    }

    data class FolderInfo(
        val path: String,
        val name: String,
        val musicCount: Int
    )

    // 폴더 내의 모든 하위 폴더에서 음악 파일 검색
    fun getMusicFromSubFolders(context: Context, rootFolderPath: String): List<MusicFile> {
        val allMusic = mutableListOf<MusicFile>()

        File(rootFolderPath).walkTopDown().forEach { file ->
            if (file.isDirectory) {
                allMusic.addAll(getMusicFromFolder(context, file.absolutePath))
            }
        }

        return allMusic
    }
}

@Composable
fun Greeting(map: MutableMap<String, MutableState<Boolean>>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LazyColumn (
        //modifier = Modifier.fillMaxWidth()
        modifier = modifier
    ) {
        val list=map.keys.toList()
        items(list) { item ->
        //items(map.size) { item ->
        //map.forEach { (t, u) ->
            val checked: MutableState<Boolean> = map[item]!!
            //var checkedState = remember { mutableStateOf(map[item] == true) }
            val onCheckedChange: (Boolean) -> Unit = {
                //Toast.makeText(context, "$t $it", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "$item $it", Toast.LENGTH_SHORT).show()
                checked.value = it
            }
            MyRow(checked, onCheckedChange, item)
        }
    }
}

@Composable
private fun MyRow(
    checked: MutableState<Boolean>,
    onCheckedChange: (Boolean) -> Unit,
    item: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { // 행 전체 클릭 반응
            }
    ) {
        Checkbox(
            //checked = u.value
            checked = checked.value,
            onCheckedChange = onCheckedChange
        )
        Text(
            //text = t
            text = item
        )
    }
}
















