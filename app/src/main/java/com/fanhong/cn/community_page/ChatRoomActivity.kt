package com.fanhong.cn.community_page

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import com.fanhong.cn.App
import com.fanhong.cn.R
import com.fanhong.cn.community_page.subsidiary.CommunityChatAdapter
import com.fanhong.cn.community_page.subsidiary.CommunityMessageBean
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.UserInfo
import io.rong.message.TextMessage
import kotlinx.android.synthetic.main.activity_chat_room.*
import java.util.*

class ChatRoomActivity : AppCompatActivity() {


    private var pref: SharedPreferences? = null
    private var token = ""
    private var roomId = ""

    private var ATCHATROOM: Boolean = false

    private var mMessagelist: MutableList<CommunityMessageBean> = ArrayList()
    private lateinit var adapter: CommunityChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        img_back.setOnClickListener { finish() }

        initBaseData()
        connectRongIM(token, roomId)
        btn_msg_send.setOnClickListener(sendListener)

        optimizeViews()
    }

    private fun initBaseData() {
        pref = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE)
        token = pref!!.getString(App.PrefNames.TOKEN, "")
        roomId = pref!!.getString(App.PrefNames.GARDENID, "")
        tv_chat_title.text = pref!!.getString(App.PrefNames.GARDENNAME, "")
    }

    private fun optimizeViews() {
        btn_msg_send.isEnabled = false
        edt_chat_input.isEnabled = false
        lv_chatRoom_msg_content.setOnItemClickListener { _, _, _, _ -> edt_chat_input.clearFocus() }
        edt_chat_input.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                btn_msg_send.isEnabled = !TextUtils.isEmpty(edt_chat_input.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        lv_chatRoom_msg_content.setOnScrollListener(object :AbsListView.OnScrollListener{
            var totaItemCounts: Int = 0
            var lasstVisible: Int = 0
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                //最后一个可见item是第一个可见item加上所有可见item（第一个可见item为0）
                this.lasstVisible = firstVisibleItem + visibleItemCount
                //所有item为listview的所有item
                this.totaItemCounts = totalItemCount
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (totaItemCounts == lasstVisible && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    edt_chat_input.isFocusable = true
                    edt_chat_input.isFocusableInTouchMode = true
                    edt_chat_input.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }

        })
    }

    /**
     * 连接融云服务器并加入指定聊天室
     *
     * @param token      用户的token
     * @param chatRoomId 所选小区的聊天室ID
     */
    private fun connectRongIM(token: String, roomId: String) {
        RongIMClient.connect(token, object : RongIMClient.ConnectCallback() {
            override fun onTokenIncorrect() {
                //token失效
                edt_chat_input.isEnabled = false
                edt_chat_input.hint = "加入聊天室失败:31004"
                //                Log.i("IMFragment", "onTokenIncorrect()加入聊天室失败:31004");
            }

            override fun onSuccess(s: String) {
                //                Log.i("IMFragment", "连接聊天服务器成功");
                joinChatRoom(roomId)
            }

            override fun onError(errorCode: RongIMClient.ErrorCode) {
                edt_chat_input.isEnabled = false
                edt_chat_input.hint = "加入聊天室失败:" + errorCode.value
                //                Log.i("IMFragment", "onError连接聊天服务器失败:" + errorCode.getValue());
                if (errorCode.value == 30003) {
                    RongIMClient.getInstance().disconnect()
                    connectRongIM(token, roomId)
                }
            }
        })
    }

    /**
     * 加入聊天室
     */
    private fun joinChatRoom(roomId: String) {
        RongIMClient.getInstance().joinChatRoom(roomId, 50, object : RongIMClient.OperationCallback() {
            override fun onSuccess() {
                //初始化聊天室
                initConversation()
                ATCHATROOM = true
                //                Log.i("IMFragment", "加入聊天室成功");
            }

            override fun onError(errorCode: RongIMClient.ErrorCode) {
                edt_chat_input.isEnabled = false
                edt_chat_input.hint = "加入聊天室失败:" + errorCode.value
                //                Log.i("IMFragment", "onError连接聊天服务器失败:" + errorCode.getValue());
                if (errorCode.value == 30003)
                    joinChatRoom(roomId)

            }
        })
    }

    /**
     * 初始化聊天室：添加欢迎消息、拉取历史信息、设置消息接收监听
     */
    private fun initConversation() {
        mMessagelist.clear()
        mMessagelist.add(CommunityMessageBean("assets://images/systmsghead.png", pref!!.getString(App.PrefNames.GARDENNAME, "帆社区"),
                "欢迎加入我们的聊天室", System.currentTimeMillis(), CommunityMessageBean.TYPE_LEFT))
        updateListTime()
        adapter = CommunityChatAdapter(this, mMessagelist)
        lv_chatRoom_msg_content.adapter = adapter
        edt_chat_input.isEnabled = true
        edt_chat_input.hint = ""
        /**RongIMClient.getInstance().getChatroomHistoryMessages
         * 聊天室中该方法是付费功能，需要开通才能使用
         * targetId - 目标 Id。根据不同的 conversationType，可能是用户 Id、讨论组 Id、群组 Id。
         * recordTime - 起始的消息发送时间戳，单位: 毫秒。
         * count - 要获取的消息数量，count 大于 0 ，小于等于 200。
         * order - 拉取顺序: 降序, 按照时间戳从大到小; 升序, 按照时间戳从小到大。
         */
        /**
        //        RongIMClient.getInstance().getChatroomHistoryMessages(SampleConnection.CHATROOM, 0, 50, RongIMClient.TimestampOrder.RC_TIMESTAMP_DESC, new IRongCallback.IChatRoomHistoryMessageCallback() {
        //            @Override
        //            public void onSuccess(List<Message> list, long l) {
        //                for (Message message : list) {
        //                    if (message != null) {
        //                        CommunityMessageBean bean = new CommunityMessageBean();
        //                        TextMessage msg = (TextMessage) message.getContent();
        //                        UserInfo info = message.getContent().getUserInfo();//获取消息体中所附带的用户信息
        //                        //获取消息内容
        //                        bean.setMessage(msg.getContent());
        //                        bean.setMsgTime(message.getSentTime());
        //                        bean.setUserName(info.getName());
        //                        bean.setHeadUrl(SampleConnection.getUrlFromUri(info.getPortraitUri()));
        //                        //对比消息的用户是否是当前用户，是则显示在右边，否则显示在左边
        //                        if (getCurrentInfo().getUserId().equals(info.getUserId()))
        //                            bean.setType(CommunityMessageBean.TYPE_RIGHT);
        //                        else
        //                            bean.setType(CommunityMessageBean.TYPE_LEFT);
        //                        mMessagelist.add(bean);//在UI线程中更新
        //                        parentActivity.runOnUiThread(new Runnable() {
        //
        //                            @Override
        //                            public void run() {
        //                                adapter.notifyDataSetChanged();
        //                            }
        //                        });
        //                    }
        //                }
        //            }
        //
        //            @Override
        //            public void onError(RongIMClient.ErrorCode errorCode) {
        //
        //            }
        //        });*/
        /**
         * 设置消息接收监听
         */
        RongIMClient.setOnReceiveMessageListener { message, i ->
            /**
             * 收到消息的处理。
             * @param message 收到的消息实体。
             * @param i       剩余未拉取消息数目。
             * @return 是否接收
             */
            if (message != null) {
                val bean = CommunityMessageBean()
                val msg = message.content as TextMessage
                val info = message.content.userInfo//获取消息体中所附带的用户信息
                //获取消息内容
                bean.message = msg.content
                bean.msgTime = message.sentTime
                bean.userName = info.name
                bean.headUrl = App.WEB_SITE + info.portraitUri.path
                //对比消息的用户是否是当前用户，是则显示在右边，否则显示在左边
                if (getCurrentInfo().getUserId() == info.userId)
                    bean.type = CommunityMessageBean.TYPE_RIGHT
                else
                    bean.type = CommunityMessageBean.TYPE_LEFT
                runOnUiThread {
                    mMessagelist.add(bean)//在UI线程中更新
                    updateListTime()
                    adapter.notifyDataSetChanged()
                }
            }
            true
        }
    }

    private val sendListener = View.OnClickListener {
        val input_msg = edt_chat_input.text.toString().trim { it <= ' ' }
        edt_chat_input.setText("")
        val textMessage = TextMessage.obtain(input_msg)
        textMessage.userInfo = getCurrentInfo()//在消息体中附加用户信息，以便于接收时使用
        val info = getCurrentInfo()
//        Log.i("IMFragment", "Info=:" + info.getUserId() + "," + info.getPortraitUri() + "," + info.getName());
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM, roomId, textMessage, null, null,
                object : RongIMClient.SendMessageCallback() {
                    override fun onError(integer: Int?, errorCode: RongIMClient.ErrorCode) {
                        //                        Log.i("IMFragment", "发送失败" + errorCode.getMessage());
                    }

                    override fun onSuccess(integer: Int?) {
                        //                        Log.i("IMFragment", "发送成功");
                    }
                }, object : RongIMClient.ResultCallback<Message>() {
            override fun onSuccess(message: Message) {
                val bean = CommunityMessageBean()
                val info = getCurrentInfo()//获取当前用户信息
                val msg = message.content as TextMessage
                //获取消息的内容
                bean.message = msg.content
                bean.msgTime = message.sentTime
                bean.userName = info.name
                bean.headUrl = App.WEB_SITE + info.portraitUri.path
                bean.type = CommunityMessageBean.TYPE_RIGHT//发送的消息显示在右边

                runOnUiThread {
                    mMessagelist.add(bean)
                    updateListTime()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onError(errorCode: RongIMClient.ErrorCode) {

            }
        })
    }

    /**
     * 获取当前用户的信息
     *
     * @return userInfo：包含用户ID、昵称、头像地址（地址为URI，需要处理）
     */
    private fun getCurrentInfo(): UserInfo {
        val uId = pref!!.getString(App.PrefNames.USERID, "-1")
        var nick = pref!!.getString(App.PrefNames.NICKNAME, "")
        if (TextUtils.isEmpty(nick)) nick = "匿名用户"
        val headUrl = pref!!.getString(App.PrefNames.HEADIMG, "")
        val portraitUri = Uri.parse(headUrl)
        return UserInfo(uId, nick, portraitUri)
    }

    /**
     * 将相邻消息超过5分钟的消息记录下来以便于显示
     */
    private fun updateListTime() {
        mMessagelist.indices.reversed()
                .filter { !App.old_msg_times.contains(mMessagelist[it].msgTime) && isWentMinutes(mMessagelist[it].msgTime, mMessagelist[if (it == 0) it else it - 1].msgTime, 5) }
                .forEach { App.old_msg_times.add(mMessagelist[it].msgTime) }
    }

    private fun isWentMinutes(msgtime: Long, lastmsgtime: Long, x: Int): Boolean {
        val last = Date(lastmsgtime)
        val msg = Date(msgtime)
        val went = msg.time - last.time
        return went > 1000 * 60 * x
    }

    public override fun onResume() {
        super.onResume()
        if (!ATCHATROOM)
            joinChatRoom(roomId)
    }
}
