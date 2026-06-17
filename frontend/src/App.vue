<script>
export default {
  onLaunch() {
    console.log('App Launch')
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
@import './styles/tokens.css';

page {
  background-color: #f8f8f8;
}

uni-tabbar,
.uni-tabbar,
.uni-tabbar-bottom {
  display: none !important;
  height: 0 !important;
  min-height: 0 !important;
  overflow: hidden !important;
}

uni-app.uni-app--showtabbar {
  --window-bottom: 0px !important;
  --tab-bar-height: 0px !important;
}
</style>
