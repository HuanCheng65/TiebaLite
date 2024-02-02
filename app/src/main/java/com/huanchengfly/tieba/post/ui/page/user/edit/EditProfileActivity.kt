package com.huanchengfly.tieba.post.ui.page.user.edit

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.panpf.sketch.compose.AsyncImage
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.arch.collectIn
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.TiebaLiteTheme
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.ui.widgets.compose.ActionItem
import com.huanchengfly.tieba.post.ui.widgets.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.ui.widgets.compose.BaseTextField
import com.huanchengfly.tieba.post.ui.widgets.compose.CounterTextField
import com.huanchengfly.tieba.post.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.Toolbar
import com.huanchengfly.tieba.post.ui.widgets.compose.picker.ListSinglePicker
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.registerPickMediasLauncher
import com.huanchengfly.tieba.post.utils.requestPermission
import com.huanchengfly.tieba.post.utils.shouldUsePhotoPicker
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import java.io.File

@AndroidEntryPoint
class EditProfileActivity : BaseActivity() {
    private val uCropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.send(EditProfileIntent.UploadPortrait(File(cacheDir, "cropped_portrait")))
            } else if (it.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(it.data!!)
                cropError!!.printStackTrace()
            }
        }

    private val pickMediasLauncher =
        registerPickMediasLauncher { (_, uris) ->
            if (uris.isNotEmpty()) {
                val sourceUri = uris[0]
                Glide.with(this)
                    .asFile()
                    .load(sourceUri)
                    .into(object : CustomTarget<File>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(
                            resource: File,
                            transition: Transition<in File>?
                        ) {
                            val sourceFileUri = Uri.fromFile(resource)
                            val destUri = Uri.fromFile(File(cacheDir, "cropped_portrait"))
                            val intent = UCrop.of(sourceFileUri, destUri)
                                .withAspectRatio(1f, 1f)
                                .withOptions(UCrop.Options().apply {
                                    setShowCropFrame(true)
                                    setShowCropGrid(true)
                                    setStatusBarColor(
                                        ColorUtils.getDarkerColor(
                                            ThemeUtils.getColorByAttr(
                                                this@EditProfileActivity,
                                                R.attr.colorPrimary
                                            )
                                        )
                                    )
                                    setToolbarColor(
                                        ThemeUtils.getColorByAttr(
                                            this@EditProfileActivity,
                                            R.attr.colorPrimary
                                        )
                                    )
                                    setToolbarWidgetColor(
                                        ThemeUtils.getColorByAttr(
                                            this@EditProfileActivity,
                                            R.attr.colorTextOnPrimary
                                        )
                                    )
                                    setActiveControlsWidgetColor(
                                        ThemeUtils.getColorByAttr(
                                            this@EditProfileActivity,
                                            R.attr.colorAccent
                                        )
                                    )
                                    setLogoColor(
                                        ThemeUtils.getColorByAttr(
                                            this@EditProfileActivity,
                                            R.attr.colorPrimary
                                        )
                                    )
                                    setCompressionFormat(Bitmap.CompressFormat.JPEG)
                                }
                                )
                                .getIntent(this@EditProfileActivity)
                            uCropLauncher.launch(intent)
                        }
                    })
            }
            }

    private val viewModel: EditProfileViewModel by viewModels()

    private val intents by lazy {
        merge(
            flowOf(EditProfileIntent.Init(AccountUtil.getUid() ?: "0"))
        )
    }
    private val handler = Handler(Looper.getMainLooper())
    override val isNeedImmersionBar: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TiebaLiteTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.apply {
                        setStatusBarColor(
                            Color.Transparent,
                            darkIcons = ThemeUtil.isStatusBarFontDark()
                        )
                        setNavigationBarColor(
                            Color.Transparent,
                            darkIcons = ThemeUtil.isNavigationBarFontDark()
                        )
                    }
                }
                PageEditProfile(viewModel, onBackPressed = { onBackPressed() })
            }
        }
        handler.post {
            intents.onEach(viewModel::send).launchIn(lifecycleScope)
        }
        viewModel.uiEventFlow
            .filterIsInstance<EditProfileEvent>()
            .collectIn(this) { handleEvent(it) }
    }

    private fun handleEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.Init.Fail -> {
                toastShort(event.toast)
            }

            is EditProfileEvent.Submit.Result -> {
                if (event.success) {
                    if (event.changed) toastShort(R.string.toast_success)
                    finish()
                } else {
                    toastShort(event.message)
                }
            }

            EditProfileEvent.UploadPortrait.Pick -> {
                if (shouldUsePhotoPicker()) {
                    pickMediasLauncher.launch(PickMediasRequest(mediaType = PickMediasRequest.ImageOnly))
                } else {
                    requestPermission {
                        permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            listOf(
                                PermissionUtils.READ_EXTERNAL_STORAGE,
                                PermissionUtils.WRITE_EXTERNAL_STORAGE
                            )
                        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            listOf(
                                PermissionUtils.READ_EXTERNAL_STORAGE
                            )
                        } else {
                            listOf(PermissionUtils.READ_MEDIA_IMAGES)
                        }
                        description = context.getString(R.string.tip_permission_storage)
                        onGranted = {
                            pickMediasLauncher.launch(PickMediasRequest(mediaType = PickMediasRequest.ImageOnly))
                        }
                        onDenied = {
                            toastShort(R.string.toast_no_permission_upload_portrait)
                        }
                    }
                }
            }

            is EditProfileEvent.UploadPortrait.Fail -> toastShort(event.error)
            is EditProfileEvent.UploadPortrait.Success -> toastShort(event.message)
        }
    }
}

@Composable
fun EditProfileCard(
    portrait: String,
    name: String,
    nickName: String,
    sex: Int,
    intro: String,
    //birthdayShowStatus: Boolean,
    //birthdayTime: Long,
    loading: Boolean,
    onNickNameChange: ((String) -> Unit)? = null,
    onIntroChange: ((String) -> Unit)? = null,
    onUploadPortrait: (() -> Unit)? = null,
    onModifySex: (() -> Unit)? = null,
    color: Color = ExtendedTheme.colors.background,
) {
    val context = LocalContext.current
    Card(elevation = 0.dp, backgroundColor = color) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.card_margin))
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .placeholder(visible = loading)
            ) {
                AsyncImage(
                    imageUri = StringUtil.getAvatarUrl(portrait),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    modifier = Modifier
                        .background(color = Color(0x77000000))
                        .fillMaxSize(),
                    onClick = {
                        onUploadPortrait?.invoke()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = stringResource(id = R.string.upload_portrait),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val (
                    nameTitle,
                    nameContent,
                    nickNameTitle,
                    nickNameContent,
                    sexTitle,
                    sexContent,
                    introTitle,
                    introContent,
                ) = createRefs()

                val titleEndBarrier = createEndBarrier(
                    nameTitle,
                    nickNameTitle,
                    sexTitle,
                    introTitle,
                    margin = 24.dp
                )

                Text(
                    text = stringResource(id = R.string.title_username),
                    color = ExtendedTheme.colors.textDisabled,
                    modifier = Modifier.constrainAs(nameTitle) {
                        top.linkTo(parent.top)
                        bottom.linkTo(nameContent.bottom)
                        start.linkTo(parent.start)
                    }
                )
                Text(
                    text = name,
                    color = ExtendedTheme.colors.textDisabled,
                    modifier = Modifier
                        .constrainAs(nameContent) {
                            top.linkTo(nameTitle.top)
                            bottom.linkTo(nameTitle.bottom)
                            start.linkTo(titleEndBarrier)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .placeholder(visible = loading)
                )

                Text(
                    text = stringResource(id = R.string.title_nickname),
                    color = ExtendedTheme.colors.textDisabled,
                    modifier = Modifier.constrainAs(nickNameTitle) {
                        top.linkTo(nameTitle.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                    }
                )
                Row(
                    modifier = Modifier
                        .constrainAs(nickNameContent) {
                            top.linkTo(nickNameTitle.top)
                            bottom.linkTo(nickNameTitle.bottom)
                            start.linkTo(titleEndBarrier)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .placeholder(visible = loading)
                ) {
                    BaseTextField(
                        value = nickName,
                        onValueChange = { onNickNameChange?.invoke(it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        maxLines = 1,
                    )
                }

                Text(
                    text = stringResource(id = R.string.profile_sex),
                    color = ExtendedTheme.colors.textDisabled,
                    modifier = Modifier.constrainAs(sexTitle) {
                        top.linkTo(nickNameTitle.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                    }
                )
                Row(
                    modifier = Modifier
                        .constrainAs(sexContent) {
                            top.linkTo(sexTitle.top)
                            bottom.linkTo(sexTitle.bottom)
                            start.linkTo(titleEndBarrier)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModifySex?.invoke() }
                        .placeholder(visible = loading)
                ) {
                    Text(
                        text = stringResource(
                            id = when (sex) {
                                1 -> R.string.profile_sex_male
                                2 -> R.string.profile_sex_female
                                else -> R.string.profile_sex_unset
                            }
                        ),
                        color = ExtendedTheme.colors.text,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_round_chevron_right),
                        contentDescription = null,
                        tint = ExtendedTheme.colors.textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.CenterVertically)
                    )
                }

                Text(
                    text = stringResource(id = R.string.title_intro),
                    color = ExtendedTheme.colors.textDisabled,
                    modifier = Modifier.constrainAs(introTitle) {
                        top.linkTo(sexTitle.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                    }
                )
                CounterTextField(
                    value = intro,
                    onValueChange = { onIntroChange?.invoke(it) },
                    maxLength = 500,
                    countWhitespace = false,
                    onLengthBeyondRestrict = { context.toastShort(R.string.toast_intro_length_beyond_restrict) },
                    placeholder = { Text(text = stringResource(id = R.string.tip_no_intro)) },
                    modifier = Modifier
                        .constrainAs(introContent) {
                            top.linkTo(introTitle.top)
                            start.linkTo(titleEndBarrier)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .placeholder(visible = loading),
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PageEditProfile(
    viewModel: EditProfileViewModel,
    onBackPressed: () -> Unit,
) {
    val isLoading by viewModel.uiState.collectPartialAsState(
        EditProfileState::isLoading,
        initial = false
    )
    if (!isLoading) {
        val uiState by viewModel.uiState.collectAsState()
        var sex by remember { mutableIntStateOf(uiState.sex) }
        val birthdayTime by remember { mutableLongStateOf(uiState.birthdayTime) }
        val birthdayShowStatus by remember { mutableStateOf(uiState.birthdayShowStatus) }
        var intro by remember { mutableStateOf(uiState.intro) }
        var nickName by remember { mutableStateOf(uiState.nickName) }
        Scaffold(
            topBar = {
                Toolbar(
                    title = stringResource(id = R.string.title_activity_edit_profile),
                    navigationIcon = { BackNavigationIcon { onBackPressed() } },
                    actions = {
                        ActionItem(
                            icon = ImageVector.vectorResource(id = R.drawable.ic_round_save_24),
                            contentDescription = stringResource(id = R.string.button_save_profile)
                        ) {
                            if (sex != uiState.sex ||
                                birthdayTime != uiState.birthdayTime ||
                                birthdayShowStatus != uiState.birthdayShowStatus ||
                                intro != uiState.intro ||
                                nickName != uiState.nickName
                            ) {
                                if (nickName.isNotEmpty()) {
                                    viewModel.send(
                                        EditProfileIntent.Submit(
                                            sex,
                                            birthdayTime,
                                            birthdayShowStatus,
                                            intro ?: "",
                                            nickName
                                        )
                                    )
                                }
                            } else {
                                viewModel.send(EditProfileIntent.SubmitWithoutChange)
                            }
                        }
                    }
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = dimensionResource(id = R.dimen.card_margin))
                    .imePadding()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                val dialogState = rememberDialogState()
                Dialog(
                    dialogState = dialogState,
                    title = { Text(text = stringResource(id = R.string.title_modify_sex)) },
                    buttons = {
                        DialogNegativeButton(text = stringResource(id = R.string.button_cancel))
                    }
                ) {
                    ListSinglePicker(
                        itemTitles = persistentListOf(
                            stringResource(id = R.string.profile_sex_male),
                            stringResource(id = R.string.profile_sex_female)
                        ),
                        itemValues = persistentListOf(1, 2),
                        selectedPosition = if (sex == 1) 0 else if (sex == 2) 1 else -1,
                        onItemSelected = { _, _, value, _ ->
                            sex = value
                            dismiss()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_margin)))
                EditProfileCard(
                    portrait = uiState.portrait,
                    name = uiState.name,
                    nickName = nickName,
                    sex = sex,
                    intro = intro ?: "",
                    loading = uiState.isLoading,
                    onNickNameChange = { nickName = it },
                    onIntroChange = { intro = it },
                    onUploadPortrait = { viewModel.send(EditProfileIntent.UploadPortraitStart) },
                    onModifySex = { dialogState.show = true }
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_margin)))
            }
        }
    } else {
        Scaffold(
            topBar = {
                Toolbar(
                    title = stringResource(id = R.string.title_activity_edit_profile),
                    navigationIcon = { BackNavigationIcon { onBackPressed() } }
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = dimensionResource(id = R.dimen.card_margin))
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_margin)))
                EditProfileCard(
                    portrait = "",
                    name = "",
                    nickName = "",
                    sex = 0,
                    intro = "",
                    loading = true
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_margin)))
            }
        }
    }
}

@Preview
@Composable
fun EditProfileCardPreview() {
    EditProfileCard(
        portrait = "",
        name = "test",
        nickName = "test",
        sex = 1,
        intro = "test",
        loading = false,
        color = Color.White
    )
}

@Preview
@Composable
fun EditProfileCardLoadingPreview() {
    EditProfileCard(
        portrait = "",
        name = "",
        nickName = "",
        sex = 0,
        intro = "",
        loading = true,
        color = Color.White
    )
}
