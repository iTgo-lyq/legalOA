(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-28c8d689"],{"3b3f":function(e,t,r){"use strict";r.d(t,"a",(function(){return l}));var a=r("d225"),n=r("bd86"),l=function e(){Object(a["a"])(this,e),Object(n["a"])(this,"rid",""),Object(n["a"])(this,"name",""),Object(n["a"])(this,"description",""),Object(n["a"])(this,"createBy","我"),Object(n["a"])(this,"updateBy",""),Object(n["a"])(this,"status",1),Object(n["a"])(this,"createTime",Date.now()),Object(n["a"])(this,"updateTime",Date.now()),Object(n["a"])(this,"permission",{})}},"5cd4":function(e,t,r){"use strict";r.d(t,"a",(function(){return a}));r("ac6a"),r("456d");function a(e,t,r,a,n){t=t||"id",r=r||"parentId",a=a||"children",n=n||0;var l=JSON.parse(JSON.stringify(e)),o=l.filter((function(e){var a=l.filter((function(a){return e[t]===a[r]}));return a.length>0&&(e.children=a),e[r]===n}));return""!=o?o:e}},"5d11":function(e,t,r){"use strict";r.r(t);var a=function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"app-container"},[r("el-form",{attrs:{inline:!0}},[r("el-form-item",{attrs:{label:"部门名称"}},[r("el-input",{attrs:{placeholder:"请输入部门名称",clearable:"",size:"small"},nativeOn:{keyup:function(t){return!t.type.indexOf("key")&&e._k(t.keyCode,"enter",13,t.key,"Enter")?null:e.handleQuery(t)}},model:{value:e.queryParams.name,callback:function(t){e.$set(e.queryParams,"name",t)},expression:"queryParams.name"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"状态"}},[r("el-select",{attrs:{placeholder:"部门状态",clearable:"",size:"small"},model:{value:e.queryParams.status,callback:function(t){e.$set(e.queryParams,"status",t)},expression:"queryParams.status"}},e._l(e.statusOptions,(function(e,t){return r("el-option",{key:t,attrs:{label:e,value:t}})})),1)],1),e._v(" "),r("el-form-item",[r("el-button",{staticClass:"filter-item",attrs:{type:"primary",icon:"el-icon-search",size:"mini"},on:{click:e.handleQuery}},[e._v("搜索")]),e._v(" "),r("el-button",{staticClass:"filter-item",attrs:{type:"primary",icon:"el-icon-plus",size:"mini"},on:{click:e.handleAdd}},[e._v("新增")])],1)],1),e._v(" "),r("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],attrs:{data:e.deptList,"row-key":"did","default-expand-all":"","tree-props":{children:"children",hasChildren:"hasChildren"}}},[r("el-table-column",{attrs:{prop:"name",label:"部门名称","header-align":"center",width:"180"}}),e._v(" "),r("el-table-column",{attrs:{prop:"leader",label:"负责人",align:"center",width:"120"}}),e._v(" "),r("el-table-column",{attrs:{prop:"leaderRole",label:"负责人角色",align:"center",width:"120"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(e.arrayFormat(t.row.leaderRole)))]}}])}),e._v(" "),r("el-table-column",{attrs:{prop:"memberRole",label:"成员角色",align:"center",width:"120"},scopedSlots:e._u([{key:"default",fn:function(t){return[e._v(e._s(e.arrayFormat(t.row.memberRole)))]}}])}),e._v(" "),r("el-table-column",{attrs:{prop:"status",label:"状态",align:"center",formatter:e.statusFormat,width:"80"}}),e._v(" "),r("el-table-column",{attrs:{label:"创建时间",align:"center",prop:"createTime",formatter:e.timeFormat,width:"120"}}),e._v(" "),r("el-table-column",{attrs:{prop:"updateInfo",formatter:e.lastEleOfArrFormat,label:"部门动态","header-align":"center","min-width":"180"}}),e._v(" "),r("el-table-column",{attrs:{label:"操作",fixed:"right",align:"center",width:"260"},scopedSlots:e._u([{key:"default",fn:function(t){return[r("el-button",{attrs:{size:"mini",type:"text",icon:"el-icon-edit"},on:{click:function(r){return e.handleUpdate(t.row)}}},[e._v("修改")]),e._v(" "),r("el-button",{attrs:{size:"mini",type:"text",icon:"el-icon-plus"},on:{click:function(r){return e.handleAdd(t.row)}}},[e._v("新增")]),e._v(" "),0!=t.row.grade?r("el-button",{attrs:{size:"mini",type:"text",icon:"el-icon-delete"},on:{click:function(r){return e.handleDelete(t.row)}}},[e._v("删除")]):e._e()]}}])})],1),e._v(" "),r("el-dialog",{attrs:{title:e.title,visible:e.open,width:"700px","append-to-body":""},on:{"update:visible":function(t){e.open=t}}},[r("el-form",{ref:"form",attrs:{model:e.form,rules:e.rules,"label-width":"100px"}},[r("el-row",[0!==e.form.grade?r("el-col",{attrs:{span:24}},[r("el-form-item",{attrs:{label:"上级部门",prop:"superior"}},[r("treeselect",{attrs:{options:e.deptList,normalizer:e.normalizer,placeholder:"选择上级部门"},model:{value:e.form.superior,callback:function(t){e.$set(e.form,"superior",t)},expression:"form.superior"}})],1)],1):e._e(),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"部门名称",prop:"name"}},[r("el-input",{attrs:{placeholder:"请输入部门名称"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1)],1),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"负责人",prop:"leader"}},[r("el-input",{attrs:{placeholder:"请输入负责人",maxlength:"20"},model:{value:e.form.leader,callback:function(t){e.$set(e.form,"leader",t)},expression:"form.leader"}})],1)],1),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"联系电话",prop:"leaderPhone"}},[r("el-input",{attrs:{placeholder:"请输入联系电话",role:"phone",maxlength:"11"},model:{value:e.form.leaderPhone,callback:function(t){e.$set(e.form,"leaderPhone",t)},expression:"form.leaderPhone"}})],1)],1),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"邮箱",prop:"leaderMail"}},[r("el-input",{attrs:{placeholder:"请输入邮箱",role:"email",maxlength:"50"},model:{value:e.form.leaderMail,callback:function(t){e.$set(e.form,"leaderMail",t)},expression:"form.leaderMail"}})],1)],1),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"负责人角色",prop:"leaderRole"}},[r("el-select",{attrs:{multiple:"",placeholder:"负责人角色",clearable:""},model:{value:e.form.leaderRole,callback:function(t){e.$set(e.form,"leaderRole",t)},expression:"form.leaderRole"}},e._l(e.roleList,(function(e){return r("el-option",{key:e.rid,attrs:{label:e.name,value:e.name}})})),1)],1)],1),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"成员角色",prop:"memberRole"}},[r("el-select",{attrs:{multiple:"",placeholder:"成员角色",clearable:""},model:{value:e.form.memberRole,callback:function(t){e.$set(e.form,"memberRole",t)},expression:"form.memberRole"}},e._l(e.roleList,(function(e){return r("el-option",{key:e.rid,attrs:{label:e.name,value:e.name}})})),1)],1)],1),e._v(" "),r("el-col",{attrs:{span:12}},[r("el-form-item",{attrs:{label:"部门状态"}},[r("el-radio-group",{model:{value:e.form.status,callback:function(t){e.$set(e.form,"status",t)},expression:"form.status"}},e._l(e.statusOptions,(function(t,a){return r("el-radio",{key:a,attrs:{label:a}},[e._v(e._s(t))])})),1)],1)],1)],1),e._v(" "),void 0!=e.form.did?r("el-row",[r("el-form-item",{attrs:{label:"更新备注"}},[r("el-input",{attrs:{autosize:{minRows:2,maxRows:4},type:"textarea",placeholder:"更新描述"},model:{value:e.form.lastUpdateInfo,callback:function(t){e.$set(e.form,"lastUpdateInfo",t)},expression:"form.lastUpdateInfo"}})],1)],1):e._e()],1),e._v(" "),r("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[r("el-button",{attrs:{type:"primary"},on:{click:e.submitForm}},[e._v("确 定")]),e._v(" "),r("el-button",{on:{click:e.cancel}},[e._v("取 消")])],1)],1)],1)},n=[],l=r("2d63"),o=(r("7f7f"),r("96cf"),r("3b8d")),i=r("5cd4"),s=r("ed08"),c=r("d368"),u=r("ca17"),d=r.n(u),m=(r("542c"),["停止","正常运转"]),f={name:"Department",components:{Treeselect:d.a},data:function(){return{loading:!1,list:[],deptList:[],deptOptions:[],title:"",open:!1,queryParams:{name:void 0,status:void 0},statusOptions:m,roleList:[],form:{did:"",leader:"",leaderMail:"",leaderPhone:"",name:"",status:1,superior:"0",createTime:Date.now(),leaderRole:[],memberRole:[],subordinates:[],updateInfo:[],lastUpdateInfo:"",grade:0},rules:{superior:[{required:!0,message:"上级部门不能为空",trigger:"blur"}],name:[{required:!0,message:"部门名称不能为空",trigger:"blur"}],leaderMail:[{type:"email",message:"请输入正确的邮箱地址",trigger:"blur"}],leaderPhone:[{pattern:/^1[3|4|5|6|7|8|9][0-9]\d{8}$/,message:"请输入正确的手机号码",trigger:"blur"}],leaderRole:[{required:!0,message:"角色不能为空",trigger:"blur"}],memberRole:[{required:!0,message:"角色不能为空",trigger:"blur"}]}}},created:function(){this.getList()},methods:{getList:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t,r;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.next=2,Object(c["f"])();case 2:return t=e.sent,e.next=5,Object(c["h"])();case 5:r=e.sent,this.list=t,this.roleList=r,this.deptList=Object(i["a"])(t,"did","superior","subordinates","0");case 9:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),filtList:function(){var e=Object(o["a"])(regeneratorRuntime.mark((function e(){var t,r,a,n;return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return t=this.queryParams,r=t.name,a=t.status,e.next=3,Object(c["f"])();case 3:n=e.sent,n=n.filter((function(e){return!r||-1!=e.name.indexOf(r)})).filter((function(e){return"number"!=typeof a||e.status===a})),this.list=n,console.log(n),this.deptList=Object(i["a"])(n,"did","superior","subordinates","0");case 8:case"end":return e.stop()}}),e,this)})));function t(){return e.apply(this,arguments)}return t}(),normalizer:function(e){return e.children&&!e.children.length&&delete e.children,{id:e.did,label:e.name,children:e.children}},getDept:function(e){var t,r=Object(l["a"])(this.list);try{for(r.s();!(t=r.n()).done;){var a=t.value;if(a.did==e)return a}}catch(n){r.e(n)}finally{r.f()}},statusFormat:function(e){return m[e.status]||"未知状态"},timeFormat:function(e){return Object(s["d"])(e.createTime)},arrayFormat:function(e){return e.join(" | ")},cancel:function(){this.open=!1,this.resetForm()},resetForm:function(e){this.form=Object.assign({did:void 0,leader:"",leaderMail:"",leaderPhone:"",name:"",status:1,superior:"0",createTime:Date.now(),leaderRole:[],memberRole:[],subordinates:[],updateInfo:[],lastUndateInfo:"",grade:-1},e)},handleQuery:function(){this.filtList()},handleAdd:function(e){this.resetForm(),this.$refs.form&&this.$refs.form.clearValidate(),void 0!=e&&(this.form.superior=e.did),this.open=!0,this.title="添加部门"},handleUpdate:function(e){this.resetForm(this.getDept(e.did)),this.open=!0,this.title="修改部门"},submitForm:function(){var e=this;this.$refs["form"].validate((function(t){t&&(void 0!=e.form.did?(e.form.updateInfo.push(e.form.lastUpdateInfo?e.form.lastUpdateInfo:"XX更新了部门信息"),Object(c["j"])(e.form).then((function(t){e.msgSuccess("修改成功"),e.open=!1,e.getList()}))):Object(c["a"])(e.form).then((function(t){e.msgSuccess("新增成功"),e.open=!1,e.getList()})))}))},handleDelete:function(e){var t=this;this.$confirm("是否确认删除部门"+e.name+"?","警告",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then((function(){return Object(c["d"])(e.did)})).then((function(){t.getList(),t.msgSuccess("删除成功")})).catch((function(){}))},lastEleOfArrFormat:function(e){return e.updateInfo[e.updateInfo.length-1]}}},p=f,b=r("2877"),h=Object(b["a"])(p,a,n,!1,null,null,null);t["default"]=h.exports},d368:function(e,t,r){"use strict";var a=r("75fc"),n=(r("7f7f"),r("b775")),l=(r("8c32"),r("3b3f")),o=r("d225"),i=r("bd86"),s=function e(){Object(o["a"])(this,e),Object(i["a"])(this,"did",""),Object(i["a"])(this,"name",""),Object(i["a"])(this,"leader",""),Object(i["a"])(this,"leaderMail",""),Object(i["a"])(this,"leaderPhone",""),Object(i["a"])(this,"superior","0"),Object(i["a"])(this,"status",0),Object(i["a"])(this,"grade",0),Object(i["a"])(this,"createTime",Date.now()),Object(i["a"])(this,"leaderRole",[]),Object(i["a"])(this,"memberRole",[]),Object(i["a"])(this,"subordinates",[]),Object(i["a"])(this,"updateInfo",[])};r.d(t,"g",(function(){return u})),r.d(t,"h",(function(){return d})),r.d(t,"b",(function(){return m})),r.d(t,"k",(function(){return f})),r.d(t,"c",(function(){return p})),r.d(t,"e",(function(){return b})),r.d(t,"f",(function(){return h})),r.d(t,"a",(function(){return v})),r.d(t,"j",(function(){return g})),r.d(t,"d",(function(){return O})),r.d(t,"i",(function(){return j}));var c="http://legal.tgozzz.cn/organization";function u(){return Object(n["a"])({baseURL:c,url:"/roles/permissions",method:"get"})}function d(){return Object(n["a"])({baseURL:c,url:"/roles",method:"get"})}function m(e,t){var r=Object.assign({name:"",description:"",createBy:"",updateBy:"",status:0,permission:{}},e);return r.createBy=t?t.name:"未知",Object(n["a"])({baseURL:c,url:"/roles",method:"post",data:r})}function f(e,t){var r=new l["a"];return Object.assign(r,e),r.updateTime=Date.now(),r.updateBy=t?t.name:"未知",Object(n["a"])({baseURL:c,url:"/roles/".concat(e.rid),method:"put",data:r})}function p(e,t){return Object(n["a"])({baseURL:c,url:"/roles/".concat(e,"?status=").concat(t),method:"patch"})}function b(e,t){return Object(n["a"])({baseURL:c,url:"/roles/".concat(e),method:"delete"})}function h(){return Object(n["a"])({baseURL:c,url:"/departments",method:"get"})}function v(e,t){var r=Object.assign({name:"0",leader:"0",leaderMail:"0",leaderPhone:"0",superior:"0",status:0,leaderRole:[],memberRole:[]},e);return Object(n["a"])({baseURL:c,url:"/departments",method:"post",data:r})}function g(e,t){var r=new s;return Object.assign(r,e),Object(n["a"])({baseURL:c,url:"/departments/".concat(r.did),method:"put",data:r})}function O(e,t){return Object(n["a"])({baseURL:c,url:"/departments/".concat(e),method:"delete"})}function j(e){return Object(n["a"])({baseURL:c,url:"/utils/permission/merge",method:"post",data:{roles:Object(a["a"])(e)}})}}}]);