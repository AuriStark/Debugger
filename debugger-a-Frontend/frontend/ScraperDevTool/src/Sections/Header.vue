<template>
    <q-header bordered class="bg-white" dark>
      <q-toolbar>
        <q-toolbar-title class="row">
          <img style="height: 30px" src="../assets/icon.png" />
        </q-toolbar-title>

        <div class="row q-gutter-sm">
          <div class="text-weight-bold" v-bind:class="statusClass">{{ status }}</div>
        </div>
      </q-toolbar>
    </q-header>
</template>

<script>
export default {
  props:{
    connected: false
  },

  computed:{
    status: function(){
      return (this.connected? 'Connected' : 'Disconnected')
    },
    statusClass: function(){
      return (this.connected? 'positive-text' : 'negative-text')
    }
  }
    
}
</script>