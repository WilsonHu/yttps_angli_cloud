<template xmlns:v-on="http://www.w3.org/1999/xhtml" xmlns:v-bind="http://www.w3.org/1999/xhtml">
    <div style="margin-left: 50px;margin-right: 50px;margin-top: 50px">
        <el-col style="background-color: white;">
            <el-row>
                <el-col>
                    <el-form :model="filters" label-position="right" label-width="85px">
                        <el-col :span="4">
                            <el-form-item label="姓名:">
                                <el-input v-model="filters.name"
                                          placeholder="姓名"
                                          auto-complete="off"
                                          clearable></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="4">
                            <el-form-item label="部门:">
                                <el-input v-model="filters.department"
                                          placeholder="部门"
                                          auto-complete="off"
                                          clearable></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="选择日期:">
                                <el-date-picker
                                        v-model="selectDate"
                                        type="daterange"
                                        align="left"
                                        unlink-panels
                                        range-separator="—"
                                        start-placeholder="开始日期"
                                        end-placeholder="结束日期"
                                        :picker-options="pickerOptions">
                                </el-date-picker>
                            </el-form-item>
                        </el-col>
                        <el-col :span="3">
                            <el-button
                                    icon="el-icon-search"
                                    size="normal"
                                    type="primary"
                                    @click="onSearch">搜索
                            </el-button>
                        </el-col>
                    </el-form>

                    <el-button style="float: right;"
                               icon="el-icon-upload2"
                               size="normal"
                               type="primary"
                               @click="exportData">导出记录
                    </el-button>


                    <el-table
                            v-loading="loadingUI"
                            element-loading-text="获取数据中..."
                            :data="tableData"
                            border
                            style="width: 100%;">
                        <el-table-column
                                width="75"
                                align="center"
                                label="序号">
                            <template scope="scope">
                                {{scope.$index+startRow}}
                            </template>
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="name"
                                label="姓名">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="department"
                                label="部门">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="staffId"
                                label="员工编号">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="recordTime"
                                label="刷卡时间">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="createTime"
                                label="创建时间">
                        </el-table-column>
                        <!--<el-table-column-->
                        <!--align="center"-->
                        <!--width="200"-->
                        <!--label="操作" >-->
                        <!--<template scope="scope" >-->
                        <!--<el-button-->
                        <!--size="small"-->
                        <!--icon="el-icon-edit"-->
                        <!--type="primary"-->
                        <!--@click="handleEdit(scope.$index, scope.row)" >编辑-->
                        <!--</el-button >-->
                        <!--</template >-->
                        <!--</el-table-column >-->
                    </el-table>
                    <div class="block" style="text-align: center; margin-top: 20px">
                        <el-pagination
                                background
                                @current-change="handleCurrentChange"
                                :current-page="currentPage"
                                :page-size="pageSize"
                                layout="total, prev, pager, next, jumper"
                                :total="totalRecords">
                        </el-pagination>
                    </div>
                </el-col>
            </el-row>
        </el-col>
    </div>
</template>

<script>
    var _this;
    export default {
        name: "record_manage",
        components: {},
        data() {
            _this = this;
            return {
                isError: false,
                errorMsg: '',
                totalRecords: 0,
                selectedItem: {},
                deleteConfirmVisible: false,
                tableData: [],
                //分页
                pageSize: EveryPageNum,//每一页的num
                currentPage: 1,
                startRow: 1,
                formLabelWidth: '100px',
                filters: {
                    name: "",
                    department: "",
                    query_start_time: "",
                    query_finish_time: ''
                },
                selectDate: [
                    dateOfThisMonth(),
                    new Date()
                ],
                loadingUI: false,
                pickerOptions: {
                    shortcuts: [{
                        text: '最近一周',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                            picker.$emit('pick', [start, end]);
                        }
                    }, {
                        text: '最近一个月',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
                            picker.$emit('pick', [start, end]);
                        }
                    }, {
                        text: '最近三个月',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
                            picker.$emit('pick', [start, end]);
                        }
                    }]
                },
            }
        },
        methods: {

            handleSizeChange(val) {
            },
            handleCurrentChange(val) {
                this.currentPage = val;
                this.onSearch();
            },
            onSearch() {
                _this.loadingUI = true;
                _this.filters.page = _this.currentPage;
                _this.filters.size = _this.pageSize;
                if (_this.selectDate != null && _this.selectDate.length > 0) {
                    _this.filters.query_start_time = _this.selectDate[0].format("yyyy-MM-dd");
                    let date = new Date();
                    date.setDate(_this.selectDate[1].getDate() + 1);
                    _this.filters.query_finish_time = date.format("yyyy-MM-dd");
                }
                $.ajax({
                    url: HOST + "/record/search",
                    type: 'POST',
                    dataType: 'json',
                    data: _this.filters,
                    success: function (data) {
                        if (data.code == 200) {
                            _this.totalRecords = data.data.total;
                            _this.tableData = data.data.list;
                            _this.startRow = data.data.startRow;
                        }
                        _this.loadingUI = false;
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                        _this.loadingUI = false;
                    }
                })
            },
            exportData() {
                _this.loadingUI = true;
                _this.filters.page = _this.currentPage;
                _this.filters.size = _this.pageSize;
                if (_this.selectDate != null && _this.selectDate.length > 0) {
                    _this.filters.query_start_time = _this.selectDate[0].format("yyyy-MM-dd");
                    let date = new Date();
                    date.setDate(_this.selectDate[1].getDate() + 1);
                    _this.filters.query_finish_time = date.format("yyyy-MM-dd");
                }
                $.ajax({
                    url: HOST + "/record/exportRecord",
                    type: 'POST',
                    dataType: 'json',
                    data: _this.filters,
                    success: function (data) {
                        if (data.code == 200) {
                            downloadFile(EXCEL_URL + data.data.replace(".", ""));
                        }
                        _this.loadingUI = false;
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                        _this.loadingUI = false;
                    }
                })
            }
        },
        computed: {},
        filters: {},
        created: function () {
            this.userinfo = JSON.parse(sessionStorage.getItem('user'));
            if (isNull(this.userinfo)) {
                this.$router.push({path: '/login'});
                return;
            }
        },
        mounted: function () {
            this.onSearch();
        },
    }

</script>
<style>

</style>
