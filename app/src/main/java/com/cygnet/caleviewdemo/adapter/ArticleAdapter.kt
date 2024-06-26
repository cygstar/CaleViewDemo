package com.cygnet.caleviewdemo.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.adapter.group.GroupRecyclerAdapter

/**
 * 适配器
 * Created by huanghaibin on 2017/12/4.
 */
class ArticleAdapter(context: Context) : GroupRecyclerAdapter<String, Article>(context) {

//    private RequestManager mLoader;

    init {
//    mLoader = Glide.with(context.getApplicationContext());
        val map = LinkedHashMap<String, MutableList<Article>>()
        val titles: MutableList<String> = ArrayList()
        map["今日推荐"] = create(0)
        map["每周热点"] = create(1)
        map["最高评论"] = create(2)
        titles.add("今日推荐")
        titles.add("每周热点")
        titles.add("最高评论")
        resetGroups(map, titles)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Article, position: Int) {
        val hld = holder as ArticleViewHolder
        hld.mTextTitle.text = item.title
        hld.mTextContent.text = item.content
//      mLoader.load(item.getImgUrl()).into(h.mImageView);
    }

    override fun onCreateDefaultViewHolder(parent: ViewGroup?, type: Int): RecyclerView.ViewHolder {
        return ArticleViewHolder(mInflater.inflate(R.layout.item_list_article, parent, false))

    }

    private class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mImageView: ImageView = itemView.findViewById(R.id.imageView)
        val mTextTitle: TextView = itemView.findViewById(R.id.tv_title)
        val mTextContent: TextView = itemView.findViewById(R.id.tv_content)
    }

    companion object {
        private fun create(title: String, content: String, imgUrl: String): Article {
            val article = Article()
            article.title = title
            article.content = content
            article.imgUrl = imgUrl
            return article
        }

        private fun create(p: Int): MutableList<Article> {
            val list: MutableList<Article> = ArrayList()
            when (p) {
                0 -> {
                    list.add(
                        create(
                            "TikTok正式起诉拜登政府",
                            "TikTok周二正式起诉拜登政府，以阻止上个月通过的一项法案实施，该法案旨在迫使该应用从其中国所有者字节跳动公司(ByteDance)剥离，否则将面临封禁。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "古巴宣布对持普通护照的中国公民实施入境免签",
                            "当地时间5月4日晚，在2024年古巴旅游节的闭幕式上，古巴旅游部长胡安·卡洛斯·加西亚宣布持有普通护照的中国公民无需签证即可入境古巴。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "俄罗斯将乌克兰总统泽伦斯基列入通缉名单",
                            "俄罗斯官方媒体周六援引该国内政部数据库报道称，俄罗斯已将乌克兰总统泽伦斯基列入通缉名单。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "密歇根大学毕业典礼被亲巴抗议者打断",
                            "周六，亲巴勒斯坦抗议者短暂打断了密歇根大学的全校毕业典礼，随后被赶出现场。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "底特律上演生涯警匪追逐战",
                            "一架警用直升机拍摄到一名司机以150英里/小时的速度在底特律的高速公路上危险地穿行，最终下车逃离警察追捕的过程。",
                            "https://www.google.com/"
                        )
                    )
                }

                1 -> {
                    list.add(
                        create(
                            "福建一小米SU7刚开39公里就坏在高速",
                            "5月6日，福建厦门，一温姓男子在社交平台发视频吐槽称，自己购买的小米SU7新车刚出4S店就抛锚故障在高速，前后驾驶39公里，又要拖车回去交付中心。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "休斯敦水位超哈维飓风时水平",
                            "得州和俄克拉荷马州超过1100万人仍处于洪水警告状态，居民被敦促不要在被洪水淹没的道路上行驶。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "广东暴雨致鳄鱼逃出农场",
                            "记者从广东省中山市神湾镇农业部门获悉：受强降雨影响，神湾镇宥南村一鳄鱼养殖场外墙崩塌、场内水淹，4条鳄鱼逃至场外。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "哈马斯被曝将批准加沙停火协议",
                            "当地时间4日中午，埃及安全部门官员对外表示，巴勒斯坦伊斯兰抵抗运动（哈马斯）代表团已经到达开罗，加沙停火协议谈判取得“明显”进展。",
                            "https://www.google.com/"
                        )
                    )
                }

                2 -> {
                    list.add(
                        create(
                            "问界就山西运城M7事故中相关技术问题发布说明",
                            "2024年4月26日，在山西省侯平高速路段发生一起交通事故，一辆问界新M7 Plus在内侧快车道行驶中追尾碰撞作业中的道路养护车。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "巴菲特谈了9件重要的事",
                            "2024年伯克希尔·哈撒韦年度股东大会在北京时间5月4日晚10时15分开幕。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "全球多高校现校园亲巴抗议",
                            "学生挺巴抗议活动愈演愈烈，现在正在世界各地引起关注。其中包括伦敦、巴黎、罗马、悉尼、东京等地。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "哥伦比亚大学或取消毕业典礼",
                            "哥伦比亚大学的一名消息人士向NBC新闻透露，受连续数周巴勒斯坦支持抗议活动影响，校园安全现面临重大隐患，因此学校正在重新考虑毕业典礼计划。",
                            "https://www.google.com/"
                        )
                    )
                    list.add(
                        create(
                            "哈马斯同意停火提议",
                            "巴勒斯坦伊斯兰抵抗运动（哈马斯）6日发表声明，宣布同意斡旋方提出的加沙地带停火提议。",
                            "https://www.google.com/"
                        )
                    )
                }
            }

            return list
        }
    }

}
