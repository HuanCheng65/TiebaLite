package com.huanchengfly.tieba.post.components

//class SketchImageEngine : ImageEngine {
//    override fun loadThumbnail(
//        context: Context,
//        resize: Int,
//        placeholder: Drawable?,
//        imageView: ImageView,
//        uri: Uri?,
//    ) {
//        DisplayRequest(imageView, uri.toString()) {
//            resize(resize, resize, scale = Scale.CENTER_CROP)
//            if (placeholder != null) {
//                placeholder(placeholder)
//            }
//        }.enqueue()
//    }
//
//    override fun loadGifThumbnail(
//        context: Context,
//        resize: Int,
//        placeholder: Drawable?,
//        imageView: ImageView,
//        uri: Uri?,
//    ) {
//        DisplayRequest(imageView, uri.toString()) {
//            resize(resize, resize, scale = Scale.CENTER_CROP)
//            if (placeholder != null) {
//                placeholder(placeholder)
//            }
//        }.enqueue()
//    }
//
//    override fun loadImage(
//        context: Context?,
//        resizeX: Int,
//        resizeY: Int,
//        imageView: ImageView,
//        uri: Uri?,
//    ) {
//        DisplayRequest(imageView, uri.toString()) {
//            resize(resizeX, resizeY)
//        }.enqueue()
//    }
//
//    override fun loadLargeImage(
//        context: Context?,
//        resizeX: Int,
//        resizeY: Int,
//        imageView: SubsamplingScaleImageView?,
//        uri: Uri?,
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun loadGifImage(
//        context: Context?,
//        resizeX: Int,
//        resizeY: Int,
//        imageView: ImageView?,
//        uri: Uri?,
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun supportAnimatedGif(): Boolean = true
//}