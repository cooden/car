// pages/ranking/ranking.js
Page({
    data: {
        carList: [
            {
                "brand": "吉利",
                "subBrand": "极氪",
                "model": "极氪7X",
                "minConfig": "2025款 后驱智驾版75kWh",
                "price": "22.99万",
                "image": "/images/zeekr/极氪mix.jpg" // 示例图片，请替换为实际图片
            },
            {
                "brand": "吉利",
                "subBrand": "极氪",
                "model": "极氪MIX",
                "minConfig": "2025款 76kWh 智驾版",
                "price": "27.99万",
                "image": "/images/zeekr/极氪mix.jpg"
            },
            {
                "brand": "吉利",
                "subBrand": "吉利",
                "model": "领克900",
                "minConfig": "2025款 1.5T Halo（官方指导价）",
                "price": "30.99万",
                "image": "/images/lync_co/900.png"
            },
            {
                "brand": "比亚迪",
                "subBrand": "腾势",
                "model": "腾势N9",
                "minConfig": "2025款 易三方插混1300尊荣型",
                "price": "38.98万",
                "image": "/images/denza/n9.png" 
            }
        ]
    },

    onLoad: function () {
        // 页面加载时执行
    },

    // 其他生命周期函数或自定义函数
})