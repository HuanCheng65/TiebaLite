package com.huanchengfly.tieba.post.fragments

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import com.huanchengfly.tieba.post.interfaces.BackHandledInterface
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils
import com.huanchengfly.tieba.post.utils.HandleBackUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.coroutines.CoroutineContext

/**
 *
 *
 * Fragment基类，封装了懒加载的实现
 *
 *
 * ViewPager + Fragment 情况下，Fragment 的生命周期因 ViewPager 的缓存机制而失去了具体意义
 * 该抽象类自定义新的回调方法，当 Fragment 可见状态改变时会触发的回调方法，和 Fragment 第一次可见时会回调的方法
 *
 * @see .onFragmentVisibleChange
 * @see .onFragmentFirstVisible
 */
abstract class BaseFragment : Fragment(), BackHandledInterface, CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var unbinder: Unbinder? = null

    protected var isFragmentVisible = false
        private set
    private var isReuseView = false
    var isFirstVisible = false
        private set
    private var rootView: View? = null
    lateinit var attachContext: Context
    protected val appPreferences: AppPreferencesUtils
        get() = AppPreferencesUtils(attachContext)

    @TargetApi(23)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        onAttachToContext(context)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity)
        }
    }

    @CallSuper
    private fun onAttachToContext(context: Context) {
        attachContext = context
    }

    override fun onBackPressed(): Boolean {
        return HandleBackUtil.handleBackPress(this)
    }

    //setUserVisibleHint()在Fragment创建时会先被调用一次，传入isVisibleToUser = false
    //如果当前Fragment可见，那么setUserVisibleHint()会再次被调用一次，传入isVisibleToUser = true
    //如果Fragment从可见->不可见，那么setUserVisibleHint()也会被调用，传入isVisibleToUser = false
    //总结：setUserVisibleHint()除了Fragment的可见状态发生变化时会被回调外，在new时也会被回调
    //如果我们需要在Fragment可见与不可见时干点事，用这个的话就会有多余的回调了，那么就需要重新封装一个
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        //setUserVisibleHint()有可能在fragment的生命周期外被调用
        if (rootView == null) {
            return
        }
        if (isFirstVisible && isVisibleToUser) {
            onFragmentFirstVisible()
            isFirstVisible = false
            isFragmentVisible = true
            return
        }
        if (isVisibleToUser) {
            onFragmentVisibleChange(true)
            isFragmentVisible = true
            return
        }
        if (isFragmentVisible) {
            isFragmentVisible = false
            onFragmentVisibleChange(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVariable()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //如果setUserVisibleHint()在rootView创建前调用时，那么
        //就等到rootView创建完后才回调onFragmentVisibleChange(true)
        //保证onFragmentVisibleChange()的回调发生在rootView创建完成之后，以便支持ui操作
        if (rootView == null) {
            rootView = view
            if (userVisibleHint) {
                if (isFirstVisible) {
                    onFragmentFirstVisible()
                    isFirstVisible = false
                    isFragmentVisible = true
                    return
                }
                onFragmentVisibleChange(true)
                isFragmentVisible = true
            }
        }
        super.onViewCreated((if (isReuseView) rootView else view)!!, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        initVariable()
    }

    private fun initVariable() {
        isFirstVisible = true
        isFragmentVisible = false
        rootView = null
        isReuseView = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val inflate = inflater.inflate(getLayoutId(), container, false)
        unbinder = ButterKnife.bind(this, inflate)
        return inflate
    }


    abstract fun getLayoutId(): Int

    /**
     * 设置是否使用 view 的复用，默认开启
     * view 的复用是指，ViewPager 在销毁和重建 Fragment 时会不断调用 onCreateView() -> onDestroyView()
     * 之间的生命函数，这样可能会出现重复创建 view 的情况，导致界面上显示多个相同的 Fragment
     * view 的复用其实就是指保存第一次创建的 view，后面再 onCreateView() 时直接返回第一次创建的 view
     *
     * @param isReuse 是否使用 view 的复用
     */
    protected fun reuseView(isReuse: Boolean) {
        isReuseView = isReuse
    }

    /**
     * 去除 setUserVisibleHint() 多余的回调场景，保证只有当 Fragment 可见状态发生变化时才回调
     * 回调时机在view创建完后，所以支持ui操作，解决在setUserVisibleHint()里进行ui操作有可能报null异常的问题
     *
     *
     * 可在该回调方法里进行一些ui显示与隐藏，比如加载框的显示和隐藏
     *
     * @param isVisible true  不可见 -> 可见
     * false 可见  -> 不可见
     */
    protected open fun onFragmentVisibleChange(isVisible: Boolean) {}

    /**
     * 在 Fragment 首次可见时回调，可在这里进行加载数据，保证只在第一次打开 Fragment 时才会加载数据，
     * 这样就可以防止每次进入都重复加载数据
     * 该方法会在 onFragmentVisibleChange() 之前调用，所以第一次打开时，可以用一个全局变量表示数据下载状态，
     * 然后在该方法内将状态设置为下载状态，接着去执行下载的任务
     * 最后在 onFragmentVisibleChange() 里根据数据下载状态来控制下载进度ui控件的显示与隐藏
     */
    protected open fun onFragmentFirstVisible() {}
    open fun onAccountSwitch() {
        if (this is Refreshable) {
            (this as Refreshable).onRefresh()
        }
    }

    open fun hasOwnAppbar(): Boolean {
        return false
    }

    fun launchIO(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(IO + job, start, block)
    }

    companion object {
        private val TAG = BaseFragment::class.java.simpleName
    }
}