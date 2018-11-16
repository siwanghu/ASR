<template>
  <div class="layout">
    <Layout>
      <Header>
        <Menu mode="horizontal" theme="dark" active-name="1">
          <div>
          </div>
        </Menu>
      </Header>
      <Content :style="{padding: '0 50px'}">
        <Card>
          <div style="min-height: 820px;">
              <br>
              <Icon type="ios-analytics-outline" size="20"/>设备ID&nbsp;&nbsp;
              <Select v-model="deviceId" style="width:200px" @on-change="changeSessionId(deviceId)">
                <Option v-for="item in deviceList" :value="item.value" :key="item.value">{{ item.label }}</Option>
              </Select>&nbsp;&nbsp;&nbsp;&nbsp;
              <Icon type="ios-people-outline" size="20"/>回话ID&nbsp;&nbsp;
              <Select v-model="sessionId" style="width:200px">
                <Option v-for="item in sessionList" :value="item.value" :key="item.value">{{ item.label }}</Option>
              </Select>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <Button shape="circle" @click.native="asr">{{buttonText}}</Button><br><br>
            <div style="min-height: 820px;">
              <Card>
                <p slot="title">
                  <Breadcrumb>
                    <BreadcrumbItem to="/">
                      <Icon type="ios-analytics-outline" size="20"></Icon> {{deviceId}}
                    </BreadcrumbItem>
                    <BreadcrumbItem to="/components/breadcrumb">
                      <Icon type="ios-people-outline" size="20"></Icon> {{sessionId}}
                    </BreadcrumbItem>
                  </Breadcrumb>
                </p>
                <p><Input v-model="asrText" type="textarea" :rows="35" readonly="false"/></p>
              </Card>
            </div>
          </div>
        </Card>
        <Drawer title="登陆界面" width="500" :closable="false" :mask-closable="false" v-model="loginflag">
          <Row>
            <Icon type="ios-person" size="20"/>账号:
            <Input v-model="username" placeholder="请输入用户名" style="width: 300px" />
          </Row>
          <br><br>
          <Row>
            <Icon type="ios-key-outline" size="20"/>密码:
            <Input v-model="password" type="password" placeholder="请出入密码" style="width: 300px" />
          </Row>
          <br><br>
          <Row style="text-align:right;">
            <Button style="margin-right: 20px" size="large"  @click="loginflag=false">取消</Button>
            <Button style="margin-right: 120px" type="primary" size="large"  @click="loginServer">登陆</Button>
          </Row>
        </Drawer>
      </Content>
      <Footer class="layout-footer-center"> 版权所有©苏州蛙声科技</Footer>
    </Layout>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        asrText:"",
        username:"",
        password:"",
        asrtimer:"",
        devicetimer:"",
        loginflag:false,
        login:false,
        deviceId:"",
        sessionId:"",
        buttonText:"开始识别",
        deviceList: [],
        sessionList: [],
      }
    },
    created(){
      this.devicetimer=setInterval(this.changeDeviceId(this.deviceId),1000);
    },
    mounted() {},
    methods: {
      loginShow(){
        this.loginflag=true;
      },
      loginServer(){
        if (("" == this.username) || (""==this.password)) {
          this.$Message.error("用户名和密码不能为空!")
        } else{
          this.login=true;
          this.loginflag=false;
          this.$Message.success("登陆成功!");
        }
      },
      changeDeviceId(deviceId){
        let _self = this;
        this.$axios.get("/asr/deviceId", {
          params: {
          }
        }).then(result => {
          console.log(result);
          console.log("长度"+result.data.length);
          this.deviceList=result.data;
        }).catch(err => {});
      },
      changeSessionId(sessionId){
        let _self = this;
        this.$axios.get("/asr/sessionId", {
          params: {
            deviceId:_self.deviceId,
          }
        }).then(result => {
          console.log(result);
          console.log("长度"+result.data.length);
          this.sessionList=result.data;
        }).catch(err => {});
      },
      pullasrTest(){
        let _self = this;
        this.$axios.get("/asr/asrText", {
          params: {
            deviceIdAndSessionId:_self.deviceId+":"+_self.sessionId,
          }
        }).then(result => {
          console.log(result);
          console.log("长度"+result.data.length);
          console.log("刷新一次");
          this.asrText=result.data;
        }).catch(err => {});
      },
      asr(){
        if(this.buttonText=="开始识别") {
          if (this.deviceId == "" || this.sessionId == "") {
            this.$Message.warning("请先选择设备与对应的回话");
          } else {
            this.$Message.success("开始当前回话语音识别");
            this.buttonText = "停止识别";
            this.asrtimer=setInterval(this.pullasrTest,1000)
          }
        }else{
          this.$Message.success("停止当前回话语音识别");
          this.buttonText = "开始识别";
          clearInterval(this.asrtimer);
        }
      },
    }
  }
</script>
<style scoped>
  .layout{
    border: 1px solid #d7dde4;
    background: #f5f7f9;
    position: relative;
    border-radius: 4px;
    overflow: hidden;
  }
  .layout-logo{
    width: 100px;
    height: 30px;
    background: #5b6270;
    border-radius: 3px;
    float: left;
    position: relative;
    top: 15px;
    left: 20px;
  }
  .layout-nav{
    width: 420px;
    margin: 0 auto;
    margin-right: 20px;
  }
  .layout-footer-center{
    text-align: center;
  }
  .left {
    float: left;
    width: 400px;
    height: 820px;
  }
  .right {
    margin-left: 310px;
    height: 820px;
  }
</style>
