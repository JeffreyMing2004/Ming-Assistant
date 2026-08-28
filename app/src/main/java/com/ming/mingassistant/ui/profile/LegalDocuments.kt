package com.ming.mingassistant.ui.profile

/**
 * 用户协议 / 隐私政策 / 关于与全部合规文件：原生内置文案页。
 *
 * 文本逐字对应服务器端 HTML（server/src/main/resources/static/privacy/），
 * 不跳转网页、不加载网页，纯 Compose 原生渲染，完全离线可看。
 * 注意：若服务端文案变更，需同步更新此处。
 */

/** 文档内容块 */
internal sealed interface DocBlock {
    /** 小节标题，level=1 章节（h2）、level=2 子节（h3） */
    data class Heading(val text: String, val level: Int = 1) : DocBlock

    /** 顶部或页内说明文字（次级小字） */
    data class Meta(val text: String) : DocBlock

    /** 正文段落 */
    data class Paragraph(val text: String) : DocBlock

    /** 项目符号列表 */
    data class Bullets(val items: List<String>) : DocBlock

    /** 表格（首列为行标题，其余列以「标签：值」呈现） */
    data class Table(val headers: List<String>, val rows: List<List<String>>) : DocBlock

    /** 文档链接列表（关于页跳转到各合规子页） */
    data class Links(val items: List<DocLink>) : DocBlock
}

/** 合规子页链接 */
internal data class DocLink(
    val title: String,
    val description: String,
    val target: DocPage,
)

/** 原生文档页 */
internal data class DocPage(
    val title: String,
    val meta: String? = null,
    val blocks: List<DocBlock> = emptyList(),
)

/** 用户协议 */
internal val userAgreementPage = DocPage(
    title = "用户协议",
    meta = "更新日期：2026-08-28　生效日期：2026-08-28　欢迎使用 Ming Assistant（明鸽子 直播助手）。",
    blocks = listOf(
        DocBlock.Heading("一、协议说明"),
        DocBlock.Paragraph("本协议是您（下称“您”或“用户”）与本应用开发者（个人开发者，下称“我们”）之间就使用 Ming Assistant 应用及相关服务（下称“本服务”）所订立的契约。您注册、登录或使用本服务即视为已阅读并同意本协议全部内容。如您不同意，请停止使用本服务。"),
        DocBlock.Heading("二、服务内容"),
        DocBlock.Bullets(
            listOf(
                "开播推送：周期检查 B 站直播间「明明Uncle」的开播状态，当从未开播变为开播时向您发送系统通知；",
                "舰礼登记：记录与管理您登记的舰礼信息（昵称、联系电话、住址、B站UID、礼物类型）。登记舰礼需您在注册时填写本人B站UID；未填写本人B站UID的账号无法登记舰礼；",
                "歌单管理：记录与管理您保存的歌曲信息（歌名、歌手、备注）。",
            ),
        ),
        DocBlock.Paragraph("本服务依赖第三方平台（B站）公开接口，B站接口可能调整或不可用，我们不对第三方平台的服务稳定性承担责任。"),
        DocBlock.Heading("三、账号注册与使用"),
        DocBlock.Bullets(
            listOf(
                "您需注册账号方可使用本服务。注册时需提供用户名、密码及（选填）您的 B站 UID；",
                "您应对账号下的一切行为负责，请妥善保管账号与密码；",
                "不得恶意注册、不得利用本服务从事违反法律法规或侵害他人权益的行为。",
            ),
        ),
        DocBlock.Heading("四、用户行为规范"),
        DocBlock.Bullets(
            listOf(
                "不得利用本服务窃取、篡改或滥用他人数据；",
                "不得对服务器进行攻击、扫描、压力测试或绕过安全措施；",
                "不得上传色情、暴力、违法或侵犯他人合法权益的内容；",
                "违反上述规范，我们有权采取警示、限制功能、封禁账号等措施，并保留追究法律责任的权利。",
            ),
        ),
        DocBlock.Heading("五、个人信息保护"),
        DocBlock.Paragraph("我们高度重视您的个人信息保护，具体收集、使用与保护规则请见《隐私政策》。若本协议与隐私政策不一致，以隐私政策为准。"),
        DocBlock.Heading("六、知识产权"),
        DocBlock.Paragraph("本应用代码、名称、界面样式及原创内容的知识产权归我们所有。应用内展示的第三方内容（如直播间信息）版权归其权利人所有。"),
        DocBlock.Heading("七、免责声明"),
        DocBlock.Bullets(
            listOf(
                "本服务按“现状”提供，不对服务不中断、无错误作任何明示或默示保证；",
                "因不可抗力、网络故障、第三方服务异常等导致的损失，我们在法律允许范围内不承担责任；",
                "本应用为个人开发的免费工具，如因正常使用本服务产生纠纷，双方应友好协商解决。",
            ),
        ),
        DocBlock.Heading("八、协议变更"),
        DocBlock.Paragraph("我们可能适时修订本协议。修订后将在本页面公布并在应用内提示，修订内容自公布之日起生效。继续使用本服务视为接受修订后的协议。"),
        DocBlock.Heading("九、账号注销"),
        DocBlock.Paragraph("您可随时在应用内「个人中心 → 注销账号」发起注销。注销将删除您的账号及账号下的全部舰礼、歌单数据，且不可恢复。详情见《隐私政策 · 账号注销》。"),
        DocBlock.Heading("十、联系我们"),
        DocBlock.Paragraph("如对本协议有任何疑问，请通过 1640053235@qq.com 联系我们。"),
    ),
)

/** 隐私政策 */
internal val privacyPolicyPage = DocPage(
    title = "隐私政策",
    meta = "更新日期：2026-08-28　生效日期：2026-08-28　我们非常重视您的个人信息安全。\n本政策适用于您使用 Ming Assistant（明鸽子 直播助手）应用（下称“本应用”）及由个人开发者提供的相关服务（下称“本服务”）时的个人信息处理活动。请您在使用前仔细阅读并充分理解本政策。如您有任何疑问，可联系 1640053235@qq.com。",
    blocks = listOf(
        DocBlock.Heading("一、我们收集哪些信息"),
        DocBlock.Heading("1. 您主动提供的信息", level = 2),
        DocBlock.Table(
            headers = listOf("信息类型", "具体内容", "场景", "必要性"),
            rows = listOf(
                listOf("账号信息", "用户名、密码、B站UID（选填）", "注册账号", "必要（B站UID选填）"),
                listOf("登录凭证", "用户名、密码", "登录账号", "必要"),
                listOf("舰礼登记信息", "昵称、联系电话、住址、B站UID（选填）、礼物类型", "新增舰礼登记", "用户主动填写，按需提供"),
                listOf("歌单信息", "歌名、歌手（选填）、备注（选填）", "保存歌曲", "用户主动填写，按需提供"),
            ),
        ),
        DocBlock.Heading("2. 我们访问与生成的信息", level = 2),
        DocBlock.Table(
            headers = listOf("信息类型", "具体内容", "用途"),
            rows = listOf(
                listOf("设备本地信息", "登录令牌、用户名、用户ID、B站UID、最近一次直播状态", "保持登录状态、判断是否开播并发送通知；仅存储于您设备本地"),
                listOf("直播公开状态", "直播间房间号、开播状态、标题、在线人数、封面链接、房间地址", "开播推送与展示；来自 B站 面向公众开放的接口，非您的个人信息"),
            ),
        ),
        DocBlock.Paragraph("我们在注册/登录时收集用户名与密码，密码经安全的不可逆哈希算法（BCrypt）处理后存储，我们无法查看您的明文密码。"),
        DocBlock.Heading("二、我们如何使用信息"),
        DocBlock.Bullets(
            listOf(
                "用于创建账号、验证身份与保持登录状态；",
                "用于向您提供舰礼登记、歌单管理等核心功能；",
                "用于向您推送直播间开播通知；",
                "用于保障服务安全、排查故障与改进产品；",
                "用于响应您行使个人信息相关权利或投诉。",
            ),
        ),
        DocBlock.Paragraph("我们不会将您的个人信息用于与上述用途无关的其他目的。"),
        DocBlock.Heading("三、信息的存储与保护"),
        DocBlock.Bullets(
            listOf(
                "存储地：您的账号及内容数据存储于开发者自行部署的服务器（中国大陆境内）；设备本地信息仅存储于您的设备内。",
                "保存期限：仅在您账号存续期间及实现本政策所述目的所必需的期限内保存；注销账号后即时删除（法律另有规定的除外）。",
                "安全措施：密码采用 BCrypt 不可逆加密存储；数据传输采用令牌（JWT）鉴权机制；建议在受信网络环境下使用。",
                "泄露通知：若发生可能危害您权益的安全事件，我们将通过应用内公告、邮件等方式及时告知，并与监管要求一致地予以报告和处置。",
            ),
        ),
        DocBlock.Heading("四、信息的共享、转让与公开披露"),
        DocBlock.Paragraph("我们不与任何第三方共享、转让或公开披露您的个人信息，以下情形除外："),
        DocBlock.Bullets(
            listOf(
                "获得您明确同意后；",
                "根据法律法规、司法或行政机关的强制性要求；",
                "在法律法规允许范围内，为维护我们或其他用户的合法权益所必需。",
            ),
        ),
        DocBlock.Paragraph("本应用不向任何第三方 SDK、广告平台或统计平台提供您的个人信息。详见《第三方信息共享清单》与《第三方SDK清单》。"),
        DocBlock.Heading("五、您如何管理个人信息（隐私设置）"),
        DocBlock.Paragraph("我们为您提供了行使个人信息权利的多种方式："),
        DocBlock.Heading("1. 权限管理", level = 2),
        DocBlock.Paragraph("本应用仅申请保障功能所必需的权限（见《权限使用说明》）。您可随时前往设备「设置 → 应用 → Ming Assistant → 权限」中开启或关闭相应权限。关闭必要权限可能导致相关功能不可用。"),
        DocBlock.Heading("2. 个性化推荐", level = 2),
        DocBlock.Paragraph("本应用不提供任何形式的个性化推荐，不为您设置用户画像，不依据您的行为进行定向内容推送。您无需进行任何关闭个性化推荐的操作。"),
        DocBlock.Heading("3. 信息管理", level = 2),
        DocBlock.Paragraph("您可在应用内随时查看、新增或删除自己的舰礼登记与歌单数据；可在「个人中心」查看您的账号信息（用户名、B站UID、账户ID）。"),
        DocBlock.Heading("4. 信息删除", level = 2),
        DocBlock.Bullets(
            listOf(
                "单条数据：在「舰礼」「歌单」页面即可删除对应记录，删除后立即生效；",
                "全部数据与账号：见下方“账号注销”，注销后账号及全部数据将被删除且不可恢复；",
                "设备本地登录数据：退出登录即清除本地的登录令牌等会话信息。",
            ),
        ),
        DocBlock.Heading("5. 撤回同意", level = 2),
        DocBlock.Paragraph("您可以随时撤回已给予的授权或同意，方式包括但不限于："),
        DocBlock.Bullets(
            listOf(
                "关闭通知权限（撤回对推送通知的同意）；",
                "在应用内删除已登记的舰礼、歌单数据（撤回对该部分信息的处理同意）；",
                "退出登录（撤回对登录状态保持的同意）；",
                "注销账号（撤回对账号及全部个人信息的处理同意）；",
                "卸载并停止使用本应用。",
            ),
        ),
        DocBlock.Paragraph("撤回同意不影响撤回前基于您同意已进行的个人信息处理活动的合法性。"),
        DocBlock.Heading("六、账号注销"),
        DocBlock.Paragraph("您可以随时注销账号："),
        DocBlock.Bullets(
            listOf(
                "注销方式：打开应用 → 「个人中心」→「注销账号」，按提示确认后即完成注销；",
                "注销时效：确认注销后立即执行，无需等待；",
                "注销后果：您的账号将无法登录，账号下的用户名将可能被他人重新注册，您的舰礼登记、歌单等全部数据将从服务器删除，且不可恢复；",
                "注销前后要求：为保证账号安全，注销前请确保您的登录状态有效。注销后如需再次使用，需重新注册。",
            ),
        ),
        DocBlock.Paragraph("如您无法在应用内完成注销，可通过 1640053235@qq.com 联系我们在 15 个工作日内为您处理。"),
        DocBlock.Heading("七、未成年人保护"),
        DocBlock.Paragraph("本应用不面向未成年人提供服务。如您是未成年人，请在监护人指导下使用本服务，并由监护人对相关行为负责。若我们发现在未取得监护人同意的情况下收集了未成年人的个人信息，将设法尽快删除。"),
        DocBlock.Heading("八、信息的跨境传输"),
        DocBlock.Paragraph("您的个人信息存储于中国大陆境内服务器，我们不会将您的个人信息传输至境外。直播间开播状态取自 B站 面向公众的开放接口，仅涉及公开直播信息，不涉及您的个人信息。"),
        DocBlock.Heading("九、本政策的更新"),
        DocBlock.Paragraph("本政策可能适时更新。发生重大变更时，我们将通过应用内公告或站内信等方式通知您。如您不同意更新后的内容，可停止使用本服务或注销账号；继续使用即视为接受更新后的政策。"),
        DocBlock.Heading("十、联系我们与投诉渠道"),
        DocBlock.Bullets(
            listOf(
                "开发者：个人开发者",
                "联系方式：1640053235@qq.com",
                "响应时效：我们将在 15 个工作日内答复您关于隐私保护的询问、投诉或权利行使请求。如您对我们的处理仍有异议，可向您所在地的网信、电信等主管部门投诉举报。",
            ),
        ),
    ),
)

/** 个人信息收集清单 */
internal val collectionPage = DocPage(
    title = "个人信息收集清单",
    meta = "以下逐项列明我们在您使用本应用过程中收集的个人信息及其用途。",
    blocks = listOf(
        DocBlock.Heading("一、账号相关"),
        DocBlock.Table(
            headers = listOf("信息种类", "是否收集", "收集方式", "收集目的", "是否必需"),
            rows = listOf(
                listOf("用户名", "是", "注册/登录时由您填写", "创建与识别账号、登录", "必需"),
                listOf("密码", "是", "注册/登录时由您填写（仅存 BCrypt 哈希）", "身份验证", "必需"),
                listOf("B站UID", "可选", "注册时由您填写", "方便您在舰礼登记中快速关联B站信息", "选填"),
                listOf("账户ID、注册时间", "是", "系统自动生成", "账号管理与数据归属", "必需"),
            ),
        ),
        DocBlock.Heading("二、使用记录"),
        DocBlock.Table(
            headers = listOf("信息种类", "是否收集", "收集方式", "收集目的", "是否必需"),
            rows = listOf(
                listOf("舰礼登记：昵称", "是", "由您填写", "舰礼登记功能", "必需"),
                listOf("舰礼登记：联系电话", "是", "由您填写", "舰礼登记功能（用于联系与谢礼核对）", "必需"),
                listOf("舰礼登记：住址", "是", "由您填写", "舰礼登记功能（用于谢礼寄送）", "必需"),
                listOf("舰礼登记：B站UID", "可选", "由您填写", "舰礼登记功能", "选填"),
                listOf("舰礼登记：礼物类型", "是", "由您填写", "舰礼登记功能", "必需"),
                listOf("歌单：歌名", "是", "由您填写", "歌单管理功能", "必需"),
                listOf("歌单：歌手、备注", "可选", "由您填写", "歌单管理功能", "选填"),
                listOf("记录的创建时间", "是", "系统自动生成", "记录展示与管理", "必需"),
            ),
        ),
        DocBlock.Heading("三、设备本地信息（不离开设备）"),
        DocBlock.Table(
            headers = listOf("信息种类", "存储位置", "用途"),
            rows = listOf(
                listOf("登录令牌、用户名、用户ID、B站UID", "设备本地（DataStore）", "保持登录状态"),
                listOf("最近一次直播状态", "设备本地（DataStore）", "判断是否发生“未开播→开播”变化以触发通知"),
            ),
        ),
        DocBlock.Heading("四、不收集的信息"),
        DocBlock.Paragraph("本应用不收集：通讯录、位置、相册、相机、麦克风、通话记录、短信、设备唯一标识（如IMEI）、MAC地址、网络访问记录、传感器数据、剪贴板内容、Cookies 或任何形式的精确位置信息。"),
        DocBlock.Heading("五、说明"),
        DocBlock.Bullets(
            listOf(
                "直播开播状态（房间号、标题、在线人数等）来自 B站 面向公众开放的接口，属于公开直播信息，不属于您的个人信息；",
                "我们不以任何方式获取您的 B站 账号、密码、私信或 Cookie；",
                "登记舰礼需您在注册时填写本人 B站UID；未填写本人 B站UID 的账号无法登记舰礼，但可正常使用歌单等其他功能。",
            ),
        ),
    ),
)

/** 第三方SDK清单 */
internal val sdkPage = DocPage(
    title = "第三方SDK清单",
    meta = "如实说明本应用接入了哪些第三方 SDK 及其行为。",
    blocks = listOf(
        DocBlock.Heading("一、结论"),
        DocBlock.Paragraph("本应用未接入任何用于广告、统计、推送、崩溃上报、社交分享或地图定位的第三方 SDK。不会向任何第三方 SDK 提供您的个人信息。"),
        DocBlock.Heading("二、实际使用的第三方开源组件"),
        DocBlock.Paragraph("为提供页面渲染、网络通信等基础功能，本应用依赖以下开源软件库。它们以“软件库”的形式存在于应用内，用于实现基础技术能力，不收集任何用户个人信息，不向开发者以外的任何主体传输数据，亦不包含任何第三方服务端组件："),
        DocBlock.Table(
            headers = listOf("组件", "用途", "是否收集个人信息"),
            rows = listOf(
                listOf("Jetpack Compose / Material3（AndroidX）", "界面渲染，Android 官方组件", "否"),
                listOf("Jetpack DataStore", "本地（设备内）轻量存储登录会话", "否（数据仅存本机，由您控制删除）"),
                listOf("Jetpack WorkManager", "应用内定时任务（轮询开播状态）", "否"),
                listOf("Retrofit / OkHttp", "与开发者自己的服务器进行网络请求", "否（仅与开发者服务器通信）"),
                listOf("kotlinx-serialization", "网络数据解析", "否"),
                listOf("Coil", "加载直播间封面图片", "否（图片按请求地址加载，公开直播间封面）"),
            ),
        ),
        DocBlock.Meta("注：B站 直播状态由服务器通过 B站 面向公众的开放接口查询，不经过任何 SDK；开播通知由应用本地生成并通过系统通知栏展示，不经过任何推送 SDK。"),
        DocBlock.Heading("三、与服务端的通信"),
        DocBlock.Bullets(
            listOf(
                "应用仅与开发者自有的服务器通信，完成注册、登录、舰礼/歌单数据同步、直播状态查询；",
                "直播状态查询使用 B站 公开接口，仅提交公开的直播间 UID，不涉及您的个人信息。",
            ),
        ),
        DocBlock.Heading("四、共享与披露"),
        DocBlock.Paragraph("我们不会将通过与第三方组件相关的任何信息进行共享、售卖或用于与本应用无关的用途。如未来接入新增 SDK，将同步更新本清单并在应用内提示。"),
    ),
)

/** 权限使用说明 */
internal val permissionPage = DocPage(
    title = "权限使用说明",
    meta = "本应用坚持最小权限原则，仅申请下述保障核心功能所必需的权限。",
    blocks = listOf(
        DocBlock.Table(
            headers = listOf("权限", "用途", "是否必要", "拒绝权限的影响", "撤回方式"),
            rows = listOf(
                listOf(
                    "网络访问（INTERNET）",
                    "与服务器通信：登录鉴权、同步舰礼/歌单、查询直播状态",
                    "必要",
                    "无法使用任何联网功能，应用不可用",
                    "系统「设置 → 应用 → Ming Assistant → 移动数据与Wi-Fi」中关闭网络",
                ),
                listOf(
                    "通知（POST_NOTIFICATIONS，Android 13 及以上以运行时方式申请）",
                    "在直播间「明明Uncle」开播时向您发送系统通知提醒",
                    "必要（用于开播提醒核心功能）",
                    "将无法收到开播通知，但其余功能（舰礼/歌单管理等）可正常使用",
                    "系统「设置 → 应用 → Ming Assistant → 通知」随时关闭或开启",
                ),
            ),
        ),
        DocBlock.Heading("说明"),
        DocBlock.Bullets(
            listOf(
                "本应用不申请通讯录、位置、相册、相机、麦克风、存储、电话、短信等任何其他权限；",
                "通知权限仅在您首次启动应用时申请一次；如您拒绝，应用仍可正常使用其余功能，您可在系统设置中随时开启；",
                "关闭相关权限即视为撤回相应同意，详见《隐私政策 · 撤回同意》。",
            ),
        ),
    ),
)

/** 第三方信息共享清单 */
internal val sharePage = DocPage(
    title = "第三方信息共享清单",
    meta = "如实列明我们向第三方提供您个人信息的情况。",
    blocks = listOf(
        DocBlock.Heading("一、总体结论"),
        DocBlock.Paragraph("本应用不与任何第三方共享您的个人信息。我们不向任何广告平台、统计平台、推送平台、数据分析公司提供您的个人信息。"),
        DocBlock.Heading("二、逐项共享说明"),
        DocBlock.Table(
            headers = listOf("接收方类型", "共享的字段", "用途", "目的", "是否共享"),
            rows = listOf(
                listOf("广告/营销平台", "—", "—", "—", "否"),
                listOf("统计分析平台", "—", "—", "—", "否"),
                listOf("推送服务商", "—", "—", "—", "否"),
                listOf("社交/分享平台", "—", "—", "—", "否"),
                listOf("其他第三方", "—", "—", "—", "否"),
            ),
        ),
        DocBlock.Heading("三、与 B站（哔哩哔哩）的交互说明"),
        DocBlock.Bullets(
            listOf(
                "本应用的“直播状态”功能由服务器通过 B站面向公众开放的接口查询直播间「明明Uncle」的公开状态；",
                "该查询仅提交公开的直播间账号UID，不涉及您的任何个人信息（我们不获取也不保存您的 B站 账号、密码或 Cookie）；",
                "因此，我们与 B站 之间不存在“个人信息对外提供”意义上的共享。",
            ),
        ),
        DocBlock.Heading("四、例外情形"),
        DocBlock.Paragraph("仅在以下情形下，我们可能依法向特定主体提供必要信息："),
        DocBlock.Bullets(
            listOf(
                "经您事先单独明示同意；",
                "根据法律法规、诉讼、行政或司法机关的强制性要求；",
                "为维护您或他人的生命、财产安全，或为处理紧急情况所必需。",
            ),
        ),
        DocBlock.Paragraph("如因接入新业务而产生新的信息共享，我们将通过本页面更新并在应用内提示。"),
    ),
)

/** 关于与全部合规文件（首页，含全部合规子页入口） */
internal val compliancePage = DocPage(
    title = "关于与全部合规文件",
    meta = "应用名称：Ming Assistant（明鸽子 直播助手）· 版本 v1.0 · 发布日期 2026-08-28",
    blocks = listOf(
        DocBlock.Paragraph("开发者：个人开发者"),
        DocBlock.Paragraph("联系邮箱：1640053235@qq.com"),
        DocBlock.Paragraph("隐私问题响应时效：我们将在收到您通过上述邮箱提出的隐私、协议相关问题后，15 个工作日内予以回复。"),
        DocBlock.Paragraph("Ming Assistant 是一个提供 B 站直播间「明明Uncle」开播推送、个人舰礼登记与歌单管理的个人开发桌面端/移动端应用。本页面为应用的全部法律与合规文件入口，包括："),
        DocBlock.Links(
            listOf(
                DocLink("用户协议", "使用本应用前请阅读的服务条款与双方权利义务", userAgreementPage),
                DocLink("隐私政策", "个人信息的收集、使用、存储、共享、删除与权益保障", privacyPolicyPage),
                DocLink("个人信息收集清单", "逐项列明收集哪些信息、用途与方式", collectionPage),
                DocLink("第三方SDK清单", "接入的第三方 SDK 及其用途说明", sdkPage),
                DocLink("权限使用说明", "每一项系统权限的用途与拒绝后果", permissionPage),
                DocLink("第三方信息共享清单", "是否共享、共享给谁、共享了什么", sharePage),
            ),
        ),
        DocBlock.Heading("隐私设置"),
        DocBlock.Bullets(
            listOf(
                "权限管理：通知权限可在系统「设置 → 应用 → Ming Assistant → 权限」中随时开启或关闭。",
                "个性化推荐：本应用不提供任何形式的个性化推荐，不基于您的行为进行画像或定向推送。",
                "信息管理：您可在应用内随时查看、新增或删除自己的舰礼登记与歌单数据。",
                "信息删除：删除单条记录即时生效；注销账号将删除全部账号数据（详见隐私政策“账号注销”章节）。",
                "撤回同意：您可通过关闭通知权限、删除数据、退出登录、注销账号或卸载应用的方式撤回已给予的同意。",
            ),
        ),
        DocBlock.Heading("关于「个人开发」说明"),
        DocBlock.Paragraph("本应用由个人开发者独立制作与维护，不隶属于任何公司或机构；服务器由开发者自行部署管理。作为个人开发者，我们仅处理保障应用核心功能所必需的最少个人信息，具体见《隐私政策》。"),
    ),
)