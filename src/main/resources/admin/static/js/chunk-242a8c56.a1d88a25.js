(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-242a8c56"],{"333d":function(e,t,n){"use strict";var a=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticClass:"pagination-container",class:{hidden:e.hidden}},[n("el-pagination",e._b({attrs:{background:e.background,"current-page":e.currentPage,"page-size":e.pageSize,layout:e.layout,"page-sizes":e.pageSizes,total:e.total},on:{"update:currentPage":function(t){e.currentPage=t},"update:current-page":function(t){e.currentPage=t},"update:pageSize":function(t){e.pageSize=t},"update:page-size":function(t){e.pageSize=t},"size-change":e.handleSizeChange,"current-change":e.handleCurrentChange}},"el-pagination",e.$attrs,!1))],1)},r=[];n("c5f6");Math.easeInOutQuad=function(e,t,n,a){return e/=a/2,e<1?n/2*e*e+t:(e--,-n/2*(e*(e-2)-1)+t)};var i=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(e){window.setTimeout(e,1e3/60)}}();function o(e){document.documentElement.scrollTop=e,document.body.parentNode.scrollTop=e,document.body.scrollTop=e}function s(){return document.documentElement.scrollTop||document.body.parentNode.scrollTop||document.body.scrollTop}function l(e,t,n){var a=s(),r=e-a,l=20,u=0;t="undefined"===typeof t?500:t;var c=function e(){u+=l;var s=Math.easeInOutQuad(u,a,r,t);o(s),u<t?i(e):n&&"function"===typeof n&&n()};c()}var u={name:"Pagination",props:{total:{required:!0,type:Number},page:{type:Number,default:1},limit:{type:Number,default:20},pageSizes:{type:Array,default:function(){return[10,20,30,50]}},layout:{type:String,default:"total, sizes, prev, pager, next, jumper"},background:{type:Boolean,default:!0},autoScroll:{type:Boolean,default:!0},hidden:{type:Boolean,default:!1}},computed:{currentPage:{get:function(){return this.page},set:function(e){this.$emit("update:page",e)}},pageSize:{get:function(){return this.limit},set:function(e){this.$emit("update:limit",e)}}},methods:{handleSizeChange:function(e){this.$emit("pagination",{page:this.currentPage,limit:e}),this.autoScroll&&l(0,800)},handleCurrentChange:function(e){this.$emit("pagination",{page:e,limit:this.pageSize}),this.autoScroll&&l(0,800)}}},c=u,d=(n("e498"),n("2877")),p=Object(d["a"])(c,a,r,!1,null,"6af373ef",null);t["a"]=p.exports},"3b3f":function(e,t,n){"use strict";n.d(t,"a",(function(){return i}));var a=n("d225"),r=n("bd86"),i=function e(){Object(a["a"])(this,e),Object(r["a"])(this,"rid",""),Object(r["a"])(this,"name",""),Object(r["a"])(this,"description",""),Object(r["a"])(this,"createBy","我"),Object(r["a"])(this,"updateBy",""),Object(r["a"])(this,"status",1),Object(r["a"])(this,"createTime",Date.now()),Object(r["a"])(this,"updateTime",Date.now()),Object(r["a"])(this,"permission",{})}},"4b47":function(e,t,n){"use strict";var a=n("f43c"),r=n.n(a);r.a},"5cd4":function(e,t,n){"use strict";n.d(t,"a",(function(){return a}));n("ac6a"),n("456d");function a(e,t,n,a,r){t=t||"id",n=n||"parentId",a=a||"children",r=r||0;var i=JSON.parse(JSON.stringify(e)),o=i.filter((function(e){var a=i.filter((function(a){return e[t]===a[n]}));return a.length>0&&(e.children=a),e[n]===r}));return""!=o?o:e}},7456:function(e,t,n){},a7f26:function(e,t,n){"use strict";n.r(t);var a=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticClass:"app-container"},[n("el-row",{attrs:{gutter:20}},[n("el-col",{attrs:{span:4,xs:24}},[n("div",{staticClass:"head-container"},[n("el-input",{staticStyle:{"margin-bottom":"20px"},attrs:{placeholder:"请输入部门名称",clearable:"",size:"small","prefix-icon":"el-icon-search"},model:{value:e.dept.deptName,callback:function(t){e.$set(e.dept,"deptName",t)},expression:"dept.deptName"}})],1),e._v(" "),n("div",{staticClass:"head-container"},[n("el-tree",{ref:"depTree",attrs:{data:e.dept.deptOptions,props:e.dept.defaultProps,"expand-on-click-node":!1,"filter-node-method":e.filterNode,"default-expand-all":""},on:{"node-click":e.handleNodeClick}})],1)]),e._v(" "),n("el-col",{attrs:{span:20,xs:24}},[n("el-form",{ref:"queryForm",attrs:{model:e.userQuery.queryParams,inline:!0,"label-width":"68px"}},[n("el-form-item",{attrs:{label:"用户名称",prop:"userName"}},[n("el-input",{staticStyle:{width:"240px"},attrs:{placeholder:"请输入用户名称",clearable:"",size:"small"},nativeOn:{keyup:function(t){return!t.type.indexOf("key")&&e._k(t.keyCode,"enter",13,t.key,"Enter")?null:e.handleQuery(t)}},model:{value:e.userQuery.queryParams.userName,callback:function(t){e.$set(e.userQuery.queryParams,"userName",t)},expression:"userQuery.queryParams.userName"}})],1),e._v(" "),n("el-form-item",{attrs:{label:"手机号码",prop:"phone"}},[n("el-input",{staticStyle:{width:"240px"},attrs:{placeholder:"请输入手机号码",clearable:"",size:"small"},nativeOn:{keyup:function(t){return!t.type.indexOf("key")&&e._k(t.keyCode,"enter",13,t.key,"Enter")?null:e.handleQuery(t)}},model:{value:e.userQuery.queryParams.phone,callback:function(t){e.$set(e.userQuery.queryParams,"phone",t)},expression:"userQuery.queryParams.phone"}})],1),e._v(" "),n("el-form-item",{attrs:{label:"状态",prop:"status"}},[n("el-select",{staticStyle:{width:"240px"},attrs:{placeholder:"用户状态",clearable:"",size:"small"},model:{value:e.userQuery.queryParams.status,callback:function(t){e.$set(e.userQuery.queryParams,"status",t)},expression:"userQuery.queryParams.status"}},e._l(e.userStatusOptions,(function(e,t){return n("el-option",{key:t,attrs:{label:e,value:t}})})),1)],1),e._v(" "),n("el-form-item",{attrs:{label:"创建时间"}},[n("el-date-picker",{staticStyle:{width:"240px"},attrs:{size:"small","value-format":"yyyy-MM-dd",type:"daterange","range-separator":"-","start-placeholder":"开始日期","end-placeholder":"结束日期"},model:{value:e.userQuery.queryParams.dateRange,callback:function(t){e.$set(e.userQuery.queryParams,"dateRange",t)},expression:"userQuery.queryParams.dateRange"}})],1),e._v(" "),n("el-form-item",[n("el-button",{attrs:{type:"primary",icon:"el-icon-search",size:"mini"},on:{click:e.handleQuery}},[e._v("搜索")]),e._v(" "),n("el-button",{attrs:{icon:"el-icon-refresh",size:"mini"},on:{click:e.resetQuery}},[e._v("重置")])],1)],1),e._v(" "),n("el-row",{attrs:{gutter:10}},[n("el-col",{attrs:{span:1.5}},[n("el-button",{attrs:{type:"primary",icon:"el-icon-plus",size:"mini"},on:{click:e.handleAdd}},[e._v("新增")])],1),e._v(" "),n("el-col",{attrs:{span:1.5}},[n("el-button",{attrs:{type:"success",icon:"el-icon-edit",size:"mini",disabled:e.handler.single},on:{click:e.handleUpdate}},[e._v("修改")])],1),e._v(" "),n("el-col",{attrs:{span:1.5}},[n("el-button",{attrs:{type:"danger",icon:"el-icon-delete",size:"mini",disabled:e.handler.multiple},on:{click:e.handleDeletePeople}},[e._v("删除")])],1),e._v(" "),n("el-col",{attrs:{span:1.5}},[n("el-button",{attrs:{type:"info",icon:"el-icon-upload2",size:"mini"},on:{click:e.handleImport}},[e._v("导入")])],1),e._v(" "),n("el-col",{attrs:{span:1.5}},[n("el-button",{attrs:{type:"warning",icon:"el-icon-download",size:"mini"},on:{click:e.handleExport}},[e._v("导出")])],1)],1),e._v(" "),n("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.userTable.loading,expression:"userTable.loading"}],attrs:{data:e.userTableList},on:{"selection-change":e.handleSelectionChange}},[n("el-table-column",{attrs:{type:"selection",width:"40","header-align":"center","class-name":"fix"}}),e._v(" "),n("el-table-column",{attrs:{label:"#",align:"center",width:"40"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(t.$index+1))]}}])}),e._v(" "),n("el-table-column",{attrs:{label:"称呼",align:"center",prop:"name"}}),e._v(" "),n("el-table-column",{attrs:{label:"部门",align:"center",prop:"organization.department.name"}}),e._v(" "),n("el-table-column",{attrs:{label:"手机号",align:"center",prop:"phone"}}),e._v(" "),n("el-table-column",{attrs:{label:"邮箱",align:"center",prop:"email","show-overflow-tooltip":""}}),e._v(" "),n("el-table-column",{attrs:{label:"状态",align:"center"},scopedSlots:e._u([{key:"default",fn:function(t){return[n("el-switch",{attrs:{"active-value":1,"inactive-value":0},on:{change:function(n){return e.handleStatusChange(t.row)}},model:{value:t.row.organization.status,callback:function(n){e.$set(t.row.organization,"status",n)},expression:"scope.row.organization.status"}})]}}])}),e._v(" "),n("el-table-column",{attrs:{label:"身份",align:"center",prop:"organization.role",formatter:e.rolesArrToStr}}),e._v(" "),n("el-table-column",{attrs:{label:"操作",align:"center",fixed:"right",width:"180"},scopedSlots:e._u([{key:"default",fn:function(t){return[n("el-button",{attrs:{size:"mini",type:"text",icon:"el-icon-edit"},on:{click:function(n){return e.handleUpdate(t.row)}}},[e._v("修改")]),e._v(" "),1!==t.row.userId?n("el-button",{attrs:{size:"mini",type:"text",icon:"el-icon-delete"},on:{click:function(n){return e.handleDelete(t.row)}}},[e._v("删除")]):e._e(),e._v(" "),n("el-button",{attrs:{size:"mini",type:"text",icon:"el-icon-key"},on:{click:function(n){return e.handleResetPwd(t.row)}}},[e._v("重置")])]}}])})],1),e._v(" "),n("pagination",{directives:[{name:"show",rawName:"v-show",value:e.total>0,expression:"total > 0"}],staticStyle:{float:"right"},attrs:{total:e.total,page:e.userQuery.queryParams.page,limit:e.userQuery.queryParams.pageSize},on:{"update:page":function(t){return e.$set(e.userQuery.queryParams,"page",t)},"update:limit":function(t){return e.$set(e.userQuery.queryParams,"pageSize",t)},pagination:e.getList}})],1)],1),e._v(" "),n("el-dialog",{attrs:{title:e.title,visible:e.open,width:"600px","append-to-body":""},on:{"update:visible":function(t){e.open=t}}},[n("el-form",{ref:"form",attrs:{model:e.form,rules:e.rules,"label-width":"80px"}},[n("el-row",[n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"用户名",prop:"name"}},[n("el-input",{attrs:{placeholder:"请输入用户名"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1)],1),e._v(" "),n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"手机号",prop:"phone"}},[n("el-input",{attrs:{placeholder:"请输入手机号"},model:{value:e.form.phone,callback:function(t){e.$set(e.form,"phone",t)},expression:"form.phone"}})],1)],1)],1),e._v(" "),n("el-row",[n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"年龄",prop:"age"}},[n("el-input",{attrs:{type:"number",placeholder:"年龄"},model:{value:e.form.age,callback:function(t){e.$set(e.form,"age",t)},expression:"form.age"}})],1)],1),e._v(" "),n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"性别",prop:"sex"}},[n("el-radio-group",{model:{value:e.form.sex,callback:function(t){e.$set(e.form,"sex",t)},expression:"form.sex"}},[n("el-radio",{key:0,attrs:{label:!1}},[e._v("女")]),e._v(" "),n("el-radio",{key:1,attrs:{label:!0}},[e._v("男")])],1)],1)],1)],1),e._v(" "),n("el-row",[n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"邮箱",prop:"email"}},[n("el-input",{attrs:{placeholder:"请输入邮箱"},model:{value:e.form.email,callback:function(t){e.$set(e.form,"email",t)},expression:"form.email"}})],1)],1),e._v(" "),n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"归属部门",required:""}},[n("treeselect",{attrs:{options:e.dept.deptOptions,normalizer:e.normalizer,placeholder:"请选择归属部门"},on:{close:e.handleMergeRoles},model:{value:e.form.organization.department.did,callback:function(t){e.$set(e.form.organization.department,"did",t)},expression:"form.organization.department.did"}})],1)],1)],1),e._v(" "),n("el-row",[n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"角色"}},[n("el-select",{attrs:{multiple:"",placeholder:"角色",clearable:""},on:{change:e.handleMergeRoles},model:{value:e.form.organization.roles,callback:function(t){e.$set(e.form.organization,"roles",t)},expression:"form.organization.roles"}},e._l(e.roleList,(function(e){return n("el-option",{key:e.rid,attrs:{label:e.name,value:e.name}})})),1)],1)],1),e._v(" "),n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"权限"}},[n("el-tree",{ref:"tree",attrs:{"check-strictly":!1,data:e.permissionsArray,props:e.defaultProps,"show-checkbox":"","node-key":"key"}})],1)],1)],1),e._v(" "),n("el-row",[n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"部门角色",prop:"sex"}},[n("el-radio-group",{on:{change:e.changeLeader},model:{value:e.form.organization.department.leader,callback:function(t){e.$set(e.form.organization.department,"leader",t)},expression:"form.organization.department.leader"}},[n("el-radio",{key:0,attrs:{label:!0}},[e._v("领导")]),e._v(" "),n("el-radio",{key:1,attrs:{label:!1}},[e._v("成员")])],1)],1)],1),e._v(" "),n("el-col",{attrs:{span:12}},[n("el-form-item",{attrs:{label:"状态"}},[n("el-radio-group",{model:{value:e.form.organization.status,callback:function(t){e.$set(e.form.organization,"status",t)},expression:"form.organization.status"}},e._l(e.userStatusOptions,(function(t,a){return n("el-radio",{key:a,attrs:{label:a}},[e._v(e._s(t))])})),1)],1)],1)],1)],1),e._v(" "),n("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[n("el-button",{attrs:{type:"primary"},on:{click:e.submitForm}},[e._v("确 定")]),e._v(" "),n("el-button",{on:{click:e.cancel}},[e._v("取 消")])],1)],1),e._v(" "),n("el-dialog",{attrs:{title:e.upload.title,visible:e.upload.open,width:"400px","append-to-body":""},on:{"update:visible":function(t){return e.$set(e.upload,"open",t)}}},[n("el-upload",{ref:"upload",attrs:{limit:1,accept:".xlsx",action:"#",disabled:e.upload.isUploading,"auto-upload":!1,"on-change":e.handleFileChange,drag:""}},[n("i",{staticClass:"el-icon-upload"}),e._v(" "),n("div",{staticClass:"el-upload__text"},[e._v("\n        将文件拖到此处，或\n        "),n("em",[e._v("点击上传")])]),e._v(" "),n("div",{staticClass:"el-upload__tip",attrs:{slot:"tip"},slot:"tip"},[n("el-checkbox",{model:{value:e.upload.updateSupport,callback:function(t){e.$set(e.upload,"updateSupport",t)},expression:"upload.updateSupport"}}),e._v("是否更新已经存在的用户数据\n        "),n("el-link",{staticStyle:{"font-size":"12px"},attrs:{type:"info"},on:{click:e.importTemplate}},[e._v("下载模板")])],1),e._v(" "),n("div",{staticClass:"el-upload__tip",staticStyle:{color:"red"},attrs:{slot:"tip"},slot:"tip"},[e._v("提示：仅允许导入“xlsx”格式文件！")])]),e._v(" "),n("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[n("el-button",{attrs:{type:"primary"},on:{click:e.submitFileForm}},[e._v("确 定")]),e._v(" "),n("el-button",{on:{click:function(t){e.upload.open=!1}}},[e._v("取 消")])],1)],1)],1)},r=[],i=(n("1c4c"),n("2d63")),o=(n("ac6a"),n("5df3"),n("4f7f"),n("7f7f"),n("96cf"),n("3b8d")),s=n("ed08"),l=(n("28a5"),n("e8ae")),u=n.n(l);function c(e,t){this.row=e,this.headIndex=t,this.value=function(e,n,a){var r=t[e],i=void 0;return r&&(i=this.row.getCell(r).text),"undefined"!=typeof i?a?a(i):i:n}}function d(e){var t={phone:void 0,age:void 0,sex:void 0,name:void 0,email:void 0,password:void 0,status:void 0,roles:void 0,leader:void 0,departmentName:void 0},n=e.getRow(1);return n.eachCell((function(e,n){return t[e.text]=n})),t}function p(e,t){var n=[];return e.eachRow((function(e,a){if(1!=a){var r=new c(e,t),i={phone:r.value("phone",""),age:r.value("age",99,parseInt),sex:r.value("sex",!0,(function(e){return"男"==e})),name:r.value("name",""),email:r.value("email",""),password:r.value("password","000000"),status:r.value("status",1,parseInt),roles:r.value("roles",[],(function(e){return e.trim().split(/\s+/)})),leader:r.value("leader",!1,(function(e){return"是"==e})),departmentName:r.value("departmentName","")};n.push(i)}})),n}function m(e){return function(){var t=Object(o["a"])(regeneratorRuntime.mark((function t(n){var a,r,i,o,s;return regeneratorRuntime.wrap((function(t){while(1)switch(t.prev=t.next){case 0:if(e){t.next=2;break}return t.abrupt("return");case 2:return a=n.target.result,r=new u.a.Workbook,t.next=6,r.xlsx.load(a);case 6:return i=r.getWorksheet(1),o=d(i),s=p(i,o),t.abrupt("return",e(s));case 10:case"end":return t.stop()}}),t)})));return function(e){return t.apply(this,arguments)}}()}var f=n("3786"),h=n("d368"),g=n("5cd4"),b=n("333d"),v=n("ca17"),y=n.n(v),x=(n("542c"),["小黑屋","正常"]),w={name:"User",components:{Treeselect:y.a,Pagination:b["a"]},data:function(){return{userStatusOptions:x,dept:{deptName:void 0,deptList:[],deptOptions:void 0,defaultProps:{children:"children",label:"name"}},defaultProps:{children:"children",label:"label"},userQuery:{queryParams:{page:1,pageSize:10,userName:void 0,phone:void 0,status:void 0,did:void 0,dateRange:[]}},handler:{single:!0,multiple:!0},userTable:{loading:!1,data:[],list:[],total:0},ids:[],title:"",open:!1,roleList:[],form:{uid:void 0,phone:"",age:0,sex:!0,name:"",email:"",token:"",organization:{status:1,permission:{},roles:[],department:{did:void 0,name:"",leader:!1}}},permissionsArray:[],permissionsObject:{},upload:{open:!1,title:"",isUploading:!1,updateSupport:0,url:"/prod-api/system/user/importData"},uploadDate:[],rules:{name:[{required:!0,message:"用户名称不能为空",trigger:"blur"}],email:[{type:"email",message:"'请输入正确的邮箱地址",trigger:["blur","change"]}],phone:[{required:!0,message:"手机号码不能为空",trigger:"blur"},{pattern:/^1[3|4|5|6|7|8|9][0-9]\d{8}$/,message:"请输入正确的手机号码",trigger:"blur"}]}}},computed:{deptName:function(){return this.dept.deptName},total:function(){return this.userTable.list.length},userTableList:function(){for(var e=this.userTable.list,t=this.userQuery.queryParams,n=t.page,a=t.pageSize,r=[],i=(n-1)*a;i<e.length&&i<n*a;i++)r.push(e[i]);return r}},watch:{deptName:function(e){this.$refs.deptTree.filter(e)}},created:function(){this.getTreeselect(),this.getUserList(),this.getRoles(),this.getPerm()},methods:{getUserList:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return this.loading=!0,e.next=3,Object(f["e"])();case 3:t=e.sent,this.userTable.list=t,this.userTable.data=t,this.loading=!1;case 7:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),getTreeselect:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(h["f"])();case 2:t=e.sent,this.dept.deptList=t,this.dept.deptOptions=Object(g["a"])(t,"did","superior","subordinates","0");case 5:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),getRoles:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(h["h"])();case 2:t=e.sent,this.roleList=t;case 4:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),getPerm:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t,n;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(h["i"])([]);case 2:t=e.sent,n=[],this.childrenObjToArray(t,n),this.permissionsArray=n,this.permissionsObject=t;case 7:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),normalizer:function(e){return e.children&&!e.children.length&&delete e.children,{id:e.did,label:e.name,children:e.children}},filterNode:function(e,t){return!e||-1!==t.name.indexOf(e)},handleNodeClick:function(e){this.userQuery.queryParams.did=e.id,this.getList()},handleStatusChange:function(e){var t=this,n=1==e.organization.status?"启用":"停用";this.$confirm("确认要"+n+'"'+e.name+'"用户吗?',"警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){return Object(f["b"])(e.uid,e.organization.status)})).then((function(){t.msgSuccess(n+"成功")})).catch((function(){e.status=1==e.status?0:1}))},cancel:function(){this.open=!1,this.reset()},reset:function(e){var t=this,n=Object.assign({uid:void 0,phone:"",age:0,sex:!0,name:"",email:"",token:"",organization:{status:1,permission:{},roles:[],department:{did:"",name:"",leader:!1}}},e);this.form=n;var a=e?Object(s["c"])(e.organization.permission):{},r=this.getPermissionsKey(a);this.$refs.tree?this.$refs.tree.setCheckedKeys(r):this.$nextTick((function(){t.$refs.tree.setCheckedKeys(r)}))},handleQuery:function(){this.userQuery.queryParams.page=1,this.getList()},resetQuery:function(){this.dateRange=[],this.$refs.queryForm&&this.$refs.queryForm.resetFields(),this.handleQuery()},handleSelectionChange:function(e){this.ids=e.map((function(e){return e.uid})),this.handler.single=1!=e.length,this.handler.multiple=!e.length},handleAdd:function(){this.reset(),this.open=!0,this.title="添加用户"},handleUpdate:function(e){this.reset(Object(s["c"])(e)),this.open=!0,this.title="修改用户"},getPermissionsKey:function(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:[],n=arguments.length>2?arguments[2]:void 0;for(var a in e.on&&t.push(n||"root"),e.children)if(e.children.hasOwnProperty(a)){var r=e.children[a];this.getPermissionsKey(r,t,a)}return t},handleResetPwd:function(e){var t=this;this.$prompt('请输入"'+e.name+'"的新密码',"提示",{confirmButtonText:"确定",cancelButtonText:"取消"}).then((function(n){var a=n.value;Object(f["g"])(e.uid,a).then((function(e){t.msgSuccess("修改成功，新密码是："+a)}))})).catch((function(e){return t.msgError(JSON.stringify(e))}))},handleMergeRoles:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(t){var n,a,r,o,l,u,c,d,p,m,f,g,b,v,y,x=this;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:if(n=Object(s["c"])(this.form.organization.roles),this.roleList,a=this.form.organization.department.leader,r=this.dept.deptList,o=[],l=[],u=new Set(n),"string"==typeof t){c=Object(i["a"])(r);try{for(c.s();!(d=c.n()).done;)p=d.value,p.did==t?l=a?p.leaderRole:p.memberRole:p.did==this.form.organization.department.did&&(o=a?p.leaderRole:p.memberRole)}catch(w){c.e(w)}finally{c.f()}}if("boolean"==typeof t){m=Object(i["a"])(r);try{for(m.s();!(f=m.n()).done;)g=f.value,g.did==this.form.organization.department.did&&(l=t?g.leaderRole:g.memberRole,o=t?g.memberRole:g.leaderRole)}catch(w){m.e(w)}finally{m.f()}}return o.forEach((function(e){u.delete(e)})),l.forEach((function(e){u.add(e)})),u=Array.from(u).filter((function(e){return null!=e})),this.form.organization.roles=u,e.next=15,Object(h["i"])(this.roleNameToId(u));case 15:b=e.sent,this.form.organization.permission=b,v=Object(s["c"])(b),y=this.getPermissionsKey(v),this.$refs.tree?this.$refs.tree.setCheckedKeys(y):this.$nextTick((function(){x.$refs.tree.setCheckedKeys(y)}));case 20:case"end":return e.stop()}}),e,this)})));function t(t){return e.apply(this,arguments)}return t}(),submitForm:function(){var e,t=this,n=Object(i["a"])(this.dept.deptList);try{for(n.s();!(e=n.n()).done;){var a=e.value;a.did==this.form.organization.department.did&&(this.form.organization.department.name=a.name)}}catch(r){n.e(r)}finally{n.f()}this.$refs["form"].validate(function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(n){return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:if(!n){e.next=14;break}if(void 0==t.form.uid){e.next=9;break}return e.next=4,Object(f["h"])(t.form);case 4:t.msgSuccess("修改成功"),t.open=!1,t.getUserList(),e.next=14;break;case 9:return e.next=11,Object(f["a"])(t.form);case 11:t.msgSuccess("新增成功"),t.open=!1,t.getUserList();case 14:case"end":return e.stop()}}),e)})));return function(t){return e.apply(this,arguments)}}())},handleDelete:function(e){var t=this;this.$confirm("是否确认删除用户"+e.name+"的数据项?","警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){return Object(f["c"])(e.uid)})).then((function(){t.getUserList(),t.msgSuccess("删除成功")})).catch((function(){}))},handleExport:function(){this.userQuery.queryParams;this.$confirm("是否确认导出所有用户数据项?","警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){confirm("不要点我 嘤嘤嘤")})).catch((function(e){console.log(e)}))},handleImport:function(){this.upload.title="用户导入",this.upload.open=!0},importTemplate:function(){window.location="http://qiniu.tgozzz.cn/example.xlsx"},handleFileChange:function(e,t){var n=this,a=new FileReader;a.onload=m((function(e){n.uploadDate=e})),a.readAsArrayBuffer(e.raw)},submitFileForm:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t,n=this;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return this.upload.isUploading=!0,t=this.uploadDate.map((function(e){var t={uid:void 0,phone:"",age:0,sex:!0,name:"",email:"",token:"",organization:{status:1,permission:{},roles:[],department:{did:"",name:"",leader:!1}}};t.phone=e.phone,t.email=e.email,t.name=e.name,t.password=e.password,t.sex=e.sex,t.organization.status=e.status,t.organization.department.name=e.departmentName,t.organization.department.leader=e.leader,t.organization.roles=e.roles;var a,r=Object(i["a"])(n.dept.deptList);try{for(r.s();!(a=r.n()).done;){var o=a.value;o.name==e.departmentName&&(t.organization.department.did=o.did)}}catch(s){r.e(s)}finally{r.f()}return t})),e.next=4,Object(f["a"])(t);case 4:this.upload.open=!1,this.upload.isUploading=!1,this.$refs.upload.clearFiles(),this.$alert("导入成功","导入结果",{dangerouslyUseHTMLString:!0}),this.getUserList();case 9:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),rolesArrToStr:function(e){return e.organization.roles.join(" | ")},roleNameToId:function(e){var t=this;return e.map((function(e){var n,a=Object(i["a"])(t.roleList);try{for(a.s();!(n=a.n()).done;){var r=n.value;if(r.name==e)return r.rid}}catch(o){a.e(o)}finally{a.f()}})).filter((function(e){return"undefined"!=typeof e}))},childrenObjToArray:function(e,t,n){if(e.label){var a=e.children;for(var r in e.children=[],e.key=n||"root",e.disabled=!0,a)if(a.hasOwnProperty(r)){var i=a[r];this.childrenObjToArray(i,e.children,r)}t.push(e)}},changeLeader:function(e){this.handleMergeRoles(e)},handleDeletePeople:function(){var e=this;this.$confirm("是否确认删除用户?","警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){return Object(f["d"])(e.ids)})).then((function(){e.getList(),e.msgSuccess("删除成功")})).catch((function(e){console.log(e)}))}}},O=w,k=(n("4b47"),n("2877")),_=Object(k["a"])(O,a,r,!1,null,null,null);t["default"]=_.exports},d368:function(e,t,n){"use strict";var a=n("75fc"),r=(n("7f7f"),n("b775")),i=(n("8c32"),n("3b3f")),o=n("d225"),s=n("bd86"),l=function e(){Object(o["a"])(this,e),Object(s["a"])(this,"did",""),Object(s["a"])(this,"name",""),Object(s["a"])(this,"leader",""),Object(s["a"])(this,"leaderMail",""),Object(s["a"])(this,"leaderPhone",""),Object(s["a"])(this,"superior","0"),Object(s["a"])(this,"status",0),Object(s["a"])(this,"grade",0),Object(s["a"])(this,"createTime",Date.now()),Object(s["a"])(this,"leaderRole",[]),Object(s["a"])(this,"memberRole",[]),Object(s["a"])(this,"subordinates",[]),Object(s["a"])(this,"updateInfo",[])};n.d(t,"g",(function(){return c})),n.d(t,"h",(function(){return d})),n.d(t,"b",(function(){return p})),n.d(t,"k",(function(){return m})),n.d(t,"c",(function(){return f})),n.d(t,"e",(function(){return h})),n.d(t,"f",(function(){return g})),n.d(t,"a",(function(){return b})),n.d(t,"j",(function(){return v})),n.d(t,"d",(function(){return y})),n.d(t,"i",(function(){return x}));var u="http://legal.tgozzz.cn/organization";function c(){return Object(r["a"])({baseURL:u,url:"/roles/permissions",method:"get"})}function d(){return Object(r["a"])({baseURL:u,url:"/roles",method:"get"})}function p(e,t){var n=Object.assign({name:"",description:"",createBy:"",updateBy:"",status:0,permission:{}},e);return n.createBy=t?t.name:"未知",Object(r["a"])({baseURL:u,url:"/roles",method:"post",data:n})}function m(e,t){var n=new i["a"];return Object.assign(n,e),n.updateTime=Date.now(),n.updateBy=t?t.name:"未知",Object(r["a"])({baseURL:u,url:"/roles/".concat(e.rid),method:"put",data:n})}function f(e,t){return Object(r["a"])({baseURL:u,url:"/roles/".concat(e,"?status=").concat(t),method:"patch"})}function h(e,t){return Object(r["a"])({baseURL:u,url:"/roles/".concat(e),method:"delete"})}function g(){return Object(r["a"])({baseURL:u,url:"/departments",method:"get"})}function b(e,t){var n=Object.assign({name:"0",leader:"0",leaderMail:"0",leaderPhone:"0",superior:"0",status:0,leaderRole:[],memberRole:[]},e);return Object(r["a"])({baseURL:u,url:"/departments",method:"post",data:n})}function v(e,t){var n=new l;return Object.assign(n,e),Object(r["a"])({baseURL:u,url:"/departments/".concat(n.did),method:"put",data:n})}function y(e,t){return Object(r["a"])({baseURL:u,url:"/departments/".concat(e),method:"delete"})}function x(e){return Object(r["a"])({baseURL:u,url:"/utils/permission/merge",method:"post",data:{roles:Object(a["a"])(e)}})}},e498:function(e,t,n){"use strict";var a=n("7456"),r=n.n(a);r.a},f43c:function(e,t,n){}}]);