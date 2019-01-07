package com.computer.inu.myworkinggings.Moohyeon.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.computer.inu.myworkinggings.Jemin.Adapter.BoardImageAdapter
import com.computer.inu.myworkinggings.Jemin.POST.PostResponse
import com.computer.inu.myworkinggings.Moohyeon.Adapter.DetailBoardRecyclerViewAdapter
import com.computer.inu.myworkinggings.Network.ApplicationController
import com.computer.inu.myworkinggings.R
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.activity_detail_board.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

import android.widget.TextView
import com.computer.inu.myworkinggings.Jemin.Data.ImageType
import com.computer.inu.myworkinggings.Seunghee.GET.DetailedBoardData
import com.computer.inu.myworkinggings.Seunghee.GET.GetDetailedBoardResponse
import com.computer.inu.myworkinggings.Seunghee.GET.ReplyData
import retrofit2.Callback

class DetailBoardActivity : AppCompatActivity() {
    lateinit var  detailBoardRecyclerViewAdapter : DetailBoardRecyclerViewAdapter
    private var reboardImagesList : java.util.ArrayList<MultipartBody.Part?> = java.util.ArrayList()
    var reboardImageUrlList = java.util.ArrayList<ImageType>()
    var boardId : Int = 0
    var urlSize : Int = 0
    lateinit var boardImageAdapter : BoardImageAdapter
    lateinit var requestManager : RequestManager

    val networkService: com.computer.inu.myworkinggings.Network.NetworkService by lazy {
        ApplicationController.instance.networkService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_board)


        getDetailedBoardResponse()

        boardId = intent.getIntExtra("BoardId", 0)
        requestManager = Glide.with(this)
        Log.v("asdf","전송 받은 보드 ID = " + boardId)
        //postReBoard()
        detail_board_reboard_img_recyclerview.visibility = View.GONE
        //setRecyclerView()

        detail_board_reboard_img_btn.setOnClickListener {
            val tedBottomPicker = TedBottomPicker.Builder(this@DetailBoardActivity)
                    .setOnMultiImageSelectedListener {
                        reboardUriList: java.util.ArrayList<Uri>? ->
                        reboardImageUrlList.clear()
                        for(i in 0 .. reboardUriList!!.size-1){
                            urlSize = reboardUriList!!.size-1
                            reboardUriList!!.add(reboardUriList.get(i))
                            reboardImageUrlList.add(ImageType("null",reboardUriList.get(i)))

                            val options = BitmapFactory.Options()

                            var input: InputStream? = null // here, you need to get your context.


                            input = contentResolver.openInputStream(reboardImageUrlList.get(i).imageUri)
                            val bitmap = BitmapFactory.decodeStream(input, null, options) // InputStream 으로부터 Bitmap 을 만들어 준다.
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                            val photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray())
                            val images = File(this.reboardImageUrlList.get(i).toString()) // 가져온 파일의 이름을 알아내려고 사용합니다

                            reboardImagesList.add(MultipartBody.Part.createFormData("images", images.name, photoBody))

                            for(i in 0 .. reboardImagesList.size-1){

                                Log.v("asdf", "re이미지리스트 = " + images.toString())
                            }

                            if(reboardImageUrlList.size > 0){
                                detail_board_reboard_img_recyclerview.visibility = View.VISIBLE
                                boardImageAdapter = BoardImageAdapter(reboardImageUrlList, requestManager,2,1,0)
                                detail_board_reboard_img_recyclerview.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
                                detail_board_reboard_img_recyclerview.adapter = boardImageAdapter
                            }
                            else{
                                detail_board_reboard_img_recyclerview.visibility = View.GONE
                            }
                        }
                    }
                    .setSelectMaxCount(4)
                    .showCameraTile(false)
                    .setPeekHeight(800)
                    .showTitle(false)
                    .setEmptySelectionText("선택된게 없습니다! 이미지를 선택해 주세요!")
                    .create()

            tedBottomPicker.show(supportFragmentManager)
        }

        detail_board_reboard_btn.setOnClickListener {
            if(detail_board_reboard_edit.text.toString() == ""){
                Toast.makeText(applicationContext,"내용 입력하세요.", Toast.LENGTH_LONG).show()
            }
            else{
                Log.v("asdf", "리보드 준비 완료" + detail_board_reboard_edit.text.toString())
                postReBoard()
            }
        }
    }

    private fun getDetailedBoardResponse() {


        val getDetailedBoardResponse = networkService.getDetailedBoardResponse("application/json", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjksInJvbGUiOiJVU0VSIiwiaXNzIjoiR2luZ3MgVXNlciBBdXRoIE1hbmFnZXIiLCJleHAiOjE1NDkwODg1Mjd9.P7rYzg9pNtc31--pL8qGYkC7cx2G93HhaizWlvForfg", intent.getIntExtra("BoardId", 0).toInt())

        getDetailedBoardResponse.enqueue(object : Callback<GetDetailedBoardResponse> {
            override fun onFailure(call: Call<GetDetailedBoardResponse>, t: Throwable) {
                Log.e("detailed_board fail", t.toString())
            }

            override fun onResponse(call: Call<GetDetailedBoardResponse>, response: Response<GetDetailedBoardResponse>) {
                if (response.isSuccessful) {

                    //toast(intent.getIntExtra("BoardId",0))
                    Log.v("ggg", "board list success")
                    //Toast.makeText(applicationContext,"성공",Toast.LENGTH_SHORT).show()

                    //보드연결
                    val temp: DetailedBoardData = response.body()!!.data
                    bindBoardData(temp)

                    //리보드연결
                    val reboardtemp : ArrayList<ReplyData?> = response.body()!!.data.replys
                    bindReBoardData(reboardtemp)
                }
            }

        })
    }

    private fun bindBoardData(temp: DetailedBoardData) {

        /*타이틀*/
        tv_detail_board_time.text = temp.time
        tv_detail_board_category.text = temp.category
        tv_detail_board_title.text = temp.title
        //태그
        val TagList: Array<TextView> = arrayOf(tv_detail_board_tag1,
                tv_detail_board_tag2,
                tv_detail_board_tag3,
                tv_detail_board_tag4,
                tv_detail_board_tag5
        )
        for (i in TagList.indices) {
            if (i < temp.keywords.size)
                TagList[i].text = "#" + temp.keywords[i]
            else
                TagList[i].text = null
        }

        /*contents*/
        //텍스트
        tv_detail_board_contents_text.text = temp.content
        //이미지
        lateinit var requestManager : RequestManager
        requestManager = Glide.with(this)
        for(i in 0..temp.images.size-1 )
            requestManager.load(temp.images[0]).into(iv_detail_board_contents_image)

        /* profile */
        //개인정보
        tv_item_board_profile_name.text = temp.writer
        tv_item_board_profile_role.text = temp.field
        tv_item_board_profile_team.text = temp.company
        //추천&댓글
        tv_item_board_like_cnt.text=temp.recommender.toString()
        tv_item_board_comment_cnt.text = temp.numOfReply.toString()

        //tv_item_board_profile_role.text = temp.
        //tv_item_board_profile_team.text = temp.

    }


    private fun bindReBoardData(temp : ArrayList<ReplyData?> ){

        detailBoardRecyclerViewAdapter = DetailBoardRecyclerViewAdapter(this, temp)
        rv_item_detailboard_list.adapter = detailBoardRecyclerViewAdapter
        rv_item_detailboard_list.layoutManager = LinearLayoutManager(this)
        rv_item_detailboard_list.canScrollVertically(0)

    }

    fun postReBoard() {

        var token : String = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjksInJvbGUiOiJVU0VSIiwiaXNzIjoiR2luZ3MgVXNlciBBdXRoIE1hbmFnZXIiLCJleHAiOjE1NDkwODg1Mjd9.P7rYzg9pNtc31--pL8qGYkC7cx2G93HhaizWlvForfg"

        var networkService = ApplicationController.instance.networkService
        val content = RequestBody.create(MediaType.parse("text.plain"), detail_board_reboard_edit.text.toString())

        val postReBoardResponse = networkService.postReBoard(token, content, reboardImagesList)

        Log.v("TAG", "프로젝트 생성 전송 : 토큰 = " + token + ", 내용 = " + detail_board_reboard_edit.text.toString())

        postReBoardResponse.enqueue(object : retrofit2.Callback<PostResponse>{

            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                Log.v("TAG", "통신 성공")
                if(response.isSuccessful){
                    Log.v("TAG", "보드 값 전달 성공")
                    Log.v("TAG","보드 status = " + response.body()!!.status)
                    Log.v("TAG","보드 message = " + response.body()!!.message)
                    var intent = Intent(applicationContext, DetailBoardActivity::class.java)
                    startActivity(intent)
                } else{
                    Log.v("TAG", "보드 값 전달 실패")
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable?) {
                Toast.makeText(applicationContext,"서버 연결 실패", Toast.LENGTH_SHORT).show()
            }

        })
    }

}