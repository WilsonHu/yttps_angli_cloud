<template>
    <div class="visit-bg">
        <div v-if="!dataError">
            <el-row style="margin-top: 100px">
                <el-col :span="4" :offset="10">
                    <span class="page-container">访客确认</span>
                </el-col>
            </el-row>
            <el-row type="flex" justify="center" style="margin-top: 20px">
                <el-col :span="10">
                    <el-form :model="visitorData" label-position="right" label-width="120px">
                        <el-col :span="20">
                            <el-form-item label="姓名:">
                                <el-input v-model="visitorData.name"
                                          readonly
                                          auto-complete="off">
                                </el-input>
                            </el-form-item>
                        </el-col>
                    </el-form>
                </el-col>
                <el-col :span="10">
                    <el-form :model="visitorData" label-position="right" label-width="120px">
                        <el-col :span="20">
                            <el-form-item label="手机:">
                                <el-input v-model="visitorData.phone"
                                          readonly
                                          auto-complete="off">
                                </el-input>
                            </el-form-item>
                        </el-col>
                    </el-form>
                </el-col>
            </el-row>
            <el-row type="flex" justify="center" style="margin-top: 20px">
                <el-col :span="20" :offset="2">
                    <img :src="visitorImage()"/>
                </el-col>
            </el-row>
            <el-row style="margin-top: 30px" type="flex" justify="center">
                <el-col :span="4">
                    <el-button @click="cancelVisitor()" icon="el-icon-close" type="danger">取 消</el-button>
                </el-col>
                <el-col :span="4">
                    <el-button type="primary" @click="acceptVisitor()" icon="el-icon-check">确 定</el-button>
                </el-col>
            </el-row>
        </div>
        <div v-if="dataError">
            <p class="page-container">获取访客信息失败！</p>
        </div>
    </div>
</template>
<script>
    let _this;
    var defaultImgUrl = require('../assets/img/visitor_default.jpg')
    export default {
        name: "visitor",
        components: {},
        data() {
            _this = this;
            return {
                dataError: false,
                visitorId: "",
                visitorData: {
                    name: "",
                    phone: "",
                    imageId: ""
                }
            }
        },
        methods: {
            visitorImage() {
                if(this.visitorData.imageId == "") {
                    return defaultImgUrl;
                } else {
                    return PARK_IMAGE_URL + this.visitorData.imageId;
                }
            },
            cancelVisitor() {

            },
            acceptVisitor() {

            }
        },
        created: function () {
            if (this.$route.query.visitor_id != null && this.$route.query.visitor_id != "") {
                this.visitorId = this.$route.query.visitor_id;
                $.ajax({
                    url: HOST + "/visitors/getVisitor",
                    type: 'POST',
                    dataType: 'json',
                    data:{
                        visitorId:_this.visitorId
                    },
                    success: function (data) {
                        if (data.code == 200) {
                            if(data.data.list.length > 0) {
                                _this.visitorData.name = data.data.list[0].person_information.name;
                                _this.visitorData.phone = data.data.list[0].person_information.phone;
                                _this.visitorData.imageId = data.data.list[0].face_list[0].face_image_id;
                            }
                        }
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                    }
                })
            } else {
                this.dataError = true;
            }
        },
    }

</script>
<style lang="scss" scoped>
    .visit-bg {
        overflow-y: scroll;
        height: 100%;
    }

    .page-container {
        font-size: 36px;
        text-align: center;
        color: rgb(192, 204, 218);
    }
</style>