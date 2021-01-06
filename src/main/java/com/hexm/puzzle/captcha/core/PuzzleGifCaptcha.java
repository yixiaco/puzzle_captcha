package com.hexm.puzzle.captcha.core;

import cn.hutool.core.img.gif.AnimatedGifEncoder;
import cn.hutool.core.img.gif.GifDecoder;
import cn.hutool.core.util.NumberUtil;
import com.jhlabs.image.ImageUtils;
import com.jhlabs.image.InvertAlphaFilter;
import com.jhlabs.image.ShadowFilter;
import lombok.Data;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * gif滑块验证码
 *
 * @author hexm
 * @date 2020/10/23
 */
@Data
public class PuzzleGifCaptcha {
    /** 默认宽度，用来计算阴影基本长度 */
    private static final int DEFAULT_WIDTH = 280;
    /** 随机数 */
    private static final Random RANDOM = new Random();
    /** 蒙版 */
    private static Color color = new Color(255, 255, 255, 204);
    /** alpha通道过滤器 */
    private static InvertAlphaFilter alphaFilter = new InvertAlphaFilter();
    /** 边距 */
    private static int margin = 10;

    /** 生成图片的宽度 */
    private int width = DEFAULT_WIDTH;
    /** 生成图片高度 */
    private int height = 150;
    /** x轴的坐标，由算法决定 */
    private int x;
    /** y轴的坐标，由算法决定 */
    private int y;
    /** 拼图长宽 */
    private int vwh = 10 * 3;
    /** 原图 */
    private InputStream image;
    /** 大图 */
    private ByteArrayOutputStream artwork;
    /** 小图 */
    private ByteArrayOutputStream vacancy;
    /** 是否注重速度 */
    private boolean isFast = false;
    /** 小图描边颜色 */
    private Color vacancyBorderColor;
    /** 小图描边线条的宽度 */
    private float vacancyBorderWidth = 2.5f;
    /** 主图描边的颜色 */
    private Color artworkBorderColor;
    /** 主图描边线条的宽度 */
    private float artworkBorderWidth = 5f;
    /**
     * 最高放大倍数,合理的放大倍数可以使图像平滑且提高渲染速度
     * 当isFast为false时，此属性生效
     * 放大倍数越高，生成的图像越平滑，受原始图片大小的影响。
     */
    private double maxRatio = 2;
    /**
     * 画质
     *
     * @see Image#SCALE_DEFAULT
     * @see Image#SCALE_FAST
     * @see Image#SCALE_SMOOTH
     * @see Image#SCALE_REPLICATE
     * @see Image#SCALE_AREA_AVERAGING
     */
    private int imageQuality = Image.SCALE_SMOOTH;
    /** 开启多线程处理 */
    private boolean multithreading = true;

    /**
     * 从流中读取图片
     *
     * @param is
     */
    public PuzzleGifCaptcha(InputStream is) {
        image = is;
    }

    /**
     * 从文件中读取图片
     *
     * @param fileName
     */
    public PuzzleGifCaptcha(String fileName) throws FileNotFoundException {
        image = new FileInputStream(fileName);
    }

    /**
     * 生成随机x、y坐标
     */
    private void init() {
        if (x == 0 || y == 0) {
            this.x = random(vwh, this.width - vwh - margin);
            this.y = random(margin, this.height - vwh - margin);
        }
    }

    /**
     * 执行
     */
    public void run() throws IOException {
        init();
        GifDecoder decoder = new GifDecoder();
        int status = decoder.read(image);
        if (status != GifDecoder.STATUS_OK) {
            throw new IOException("read image error!");
        }
        AnimatedGifEncoder e = new AnimatedGifEncoder();
        AnimatedGifEncoder e2 = new AnimatedGifEncoder();
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();

        //保存的目标图片
        e.start(b1);
        e2.start(b2);
        e.setRepeat(decoder.getLoopCount());
        e2.setRepeat(decoder.getLoopCount());
        e.setDelay(decoder.getDelay(0));
        e2.setDelay(decoder.getDelay(0));
        e2.setTransparent(Color.white);
        if (multithreading) {
            // 多线程
            CompletableFuture<BufferedImage[]>[] futures = new CompletableFuture[decoder.getFrameCount()];
            for (int i = 0; i < decoder.getFrameCount(); i++) {
                int finalI = i;
                futures[i] = CompletableFuture.supplyAsync(() -> {
                    BufferedImage image = decoder.getFrame(finalI);
                    //可以加入对图片的处理，比如缩放，压缩质量
                    return process(image);
                });
            }
            CompletableFuture.allOf(futures).join();
            for (CompletableFuture<BufferedImage[]> future : futures) {
                try {
                    BufferedImage[] bufferedImages = future.get();
                    e.addFrame(bufferedImages[0]);
                    e2.addFrame(bufferedImages[1]);
                } catch (InterruptedException | ExecutionException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        } else {
            // 单线程
            for (int i = 0; i < decoder.getFrameCount(); i++) {
                BufferedImage image = decoder.getFrame(i);
                //可以加入对图片的处理，比如缩放，压缩质量
                BufferedImage[] bufferedImages = process(image);
                e.addFrame(bufferedImages[0]);
                e2.addFrame(bufferedImages[1]);
            }
        }
        e.finish();
        e2.finish();
        this.artwork = b1;
        this.vacancy = b2;
        if (image != null) {
            image.close();
        }
    }

    private BufferedImage[] process(Image image) {
        // 缩略图
        Image thumbnail;
        GeneralPath path;
        int realW = image.getWidth(null);
        int realH = image.getHeight(null);
        int w = realW, h = realH;
        double wScale = 1, hScale = 1;
        // 如果原始图片比执行的图片还小，则先拉伸再裁剪
        boolean isFast = this.isFast || w < this.width || h < this.height;
        if (isFast) {
            // 缩放，使用平滑模式
            thumbnail = image.getScaledInstance(width, height, imageQuality);
            path = paintBrick(1, 1);
            w = this.width;
            h = this.height;
        } else {
            // 缩小到一定的宽高，保证裁剪的圆润
            boolean flag = false;
            if (realW > width * maxRatio) {
                // 不超过最大倍数且不超过原始图片的宽
                w = Math.min((int) (width * maxRatio), realW);
                flag = true;
            }
            if (realH > height * maxRatio) {
                h = Math.min((int) (height * maxRatio), realH);
                flag = true;
            }
            if (flag) {
                // 若放大倍数生效，则缩小图片至最高放大倍数，再进行裁剪
                thumbnail = image.getScaledInstance(w, h, imageQuality);
            } else {
                thumbnail = image;
            }
            hScale = NumberUtil.div(h, height);
            wScale = NumberUtil.div(w, width);
            path = paintBrick(wScale, hScale);
        }

        // 创建阴影过滤器
        float radius = 5 * ((float) w / DEFAULT_WIDTH) * (float) wScale;
        ShadowFilter shadowFilter = new ShadowFilter(radius, 2 * (float) wScale, -1 * (float) hScale, 0.8f);

        // 创建空白的图片
        BufferedImage artwork = translucent(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        BufferedImage localVacancy = translucent(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        // 画小图
        Graphics2D vg = localVacancy.createGraphics();
        // 抗锯齿
        vg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置画图路径范围
        vg.setClip(path);
        // 将区域中的图像画到小图中
        vg.drawImage(thumbnail, null, null);
        //描边
        if (vacancyBorderColor != null) {
            vg.setColor(vacancyBorderColor);
            vg.setStroke(new BasicStroke(vacancyBorderWidth));
            vg.draw(path);
        }
        // 画上基于小图的内阴影，先反转alpha通道，然后创建阴影
        BufferedImage shadowImage = shadowFilter.filter(alphaFilter.filter(localVacancy, null), null);
        // 画上内阴影小图
        vg.drawImage(shadowImage, null, null);
        // 释放图像
        vg.dispose();

        // 画大图
        // 创建画笔
        Graphics2D g = artwork.createGraphics();
        // 抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画上图片
        g.drawImage(thumbnail, null, null);
        // 设置画图路径范围
        g.setClip(path);
        // 填充缺口透明度 颜色混合,不透明在上
        g.setComposite(AlphaComposite.SrcAtop);
        // 填充一层白色的透明蒙版，透明度越高，白色越深 alpha：0-255
        g.setColor(color);
        g.fill(path);
        //描边
        if (artworkBorderColor != null) {
            g.setColor(artworkBorderColor);
            g.setStroke(new BasicStroke(artworkBorderWidth));
            g.draw(path);
        }
        // 画上内阴影小图
        g.drawImage(shadowImage, null, null);
        // 释放图像
        g.dispose();

        // 裁剪掉多余的透明背景
        localVacancy = ImageUtils.getSubimage(localVacancy, (int) (x * wScale), 0, (int) Math.ceil(path.getBounds().getWidth()), h);

        BufferedImage[] bufferedImages = new BufferedImage[2];
        if (isFast) {
            // 添加阴影
            bufferedImages[0] = localVacancy;
            bufferedImages[1] = artwork;
        } else {
            // 大图缩放
            bufferedImages[0] = ImageUtils.convertImageToARGB(artwork.getScaledInstance(width, height, imageQuality));
            // 缩放时，除以放大比例
            bufferedImages[1] = ImageUtils.convertImageToARGB(localVacancy.getScaledInstance((int) (path.getBounds().getWidth() / wScale), height, imageQuality));
        }
        return bufferedImages;
    }

    /**
     * 绘制拼图块的路径
     *
     * @param xScale x轴放大比例
     * @param yScale y轴放大比例
     * @return
     */
    private GeneralPath paintBrick(double xScale, double yScale) {
        double x = this.x * xScale;
        double y = this.y * yScale;
        // 直线移动的基础距离
        double hMoveL = vwh / 3f * yScale;
        double wMoveL = vwh / 3f * xScale;
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);
        path.lineTo(x + wMoveL, y);
        // 上面的圆弧正东方向0°，顺时针负数，逆时针正数
        path.append(arc(x + wMoveL, y - hMoveL / 2, wMoveL, hMoveL, 180, -180), true);
        path.lineTo(x + wMoveL * 3, y);
        path.lineTo(x + wMoveL * 3, y + hMoveL);
        // 右边的圆弧
        path.append(arc(x + wMoveL * 2 + wMoveL / 2, y + hMoveL, wMoveL, hMoveL, 90, -180), true);
        path.lineTo(x + wMoveL * 3, y + hMoveL * 3);
        path.lineTo(x, y + hMoveL * 3);
        path.lineTo(x, y + hMoveL * 2);
        // 左边的内圆弧
        path.append(arc(x - wMoveL / 2, y + hMoveL, wMoveL, hMoveL, -90, 180), true);
        path.lineTo(x, y);
        path.closePath();
        return path;
    }

    /**
     * 绘制圆形、圆弧或者是椭圆形
     * 正东方向0°，顺时针负数，逆时针正数
     *
     * @param x      左上角的x坐标
     * @param y      左上角的y坐标
     * @param w      宽
     * @param h      高
     * @param start  开始的角度
     * @param extent 结束的角度
     * @return
     */
    private Arc2D arc(double x, double y, double w, double h, double start, double extent) {
        return new Arc2D.Double(x, y, w, h, start, extent, Arc2D.OPEN);
    }

    /**
     * 透明背景
     *
     * @param bufferedImage
     * @return
     */
    private BufferedImage translucent(BufferedImage bufferedImage) {
        Graphics2D g = bufferedImage.createGraphics();
        bufferedImage = g.getDeviceConfiguration().createCompatibleImage(bufferedImage.getWidth(), bufferedImage.getHeight(), Transparency.TRANSLUCENT);
        g.dispose();
        return bufferedImage;
    }

    /**
     * 随机数
     *
     * @param min
     * @param max
     * @return
     */
    private static int random(int min, int max) {
        return RANDOM.ints(min, max + 1).findFirst().getAsInt();
    }
}
