<template>
  <q-drawer
    class="column q-gutter-y-md q-pa-md"
    v-model="opened"
    side="left"
    bordered
  >
    <q-card class="my-card text-center text-weight-medium">
      <q-card-section>
        {{ currentNode }}
      </q-card-section>
    </q-card>

    <q-list bordered class="rounded-borders">
      <q-expansion-item
        switch-toggle-side
        expand-separator
        icon="free_breakfast"
        label="Breakpoint"
        default-opened
      >
        <q-card>
          <q-card-section>
            <q-list bordered separator>
              <q-item>
                <q-item-section
                  ><q-toggle
                    class="justify-between"
                    v-model="beforeB"
                    label="Before Breakpoint"
                    left-label
                    color="info"
                    :disable="disabled"
                /></q-item-section>
              </q-item>
              <q-item>
                <q-item-section
                  ><q-toggle
                    class="justify-between"
                    v-model="afterB"
                    label="After Breakpoint"
                    left-label
                    color="warning"
                    :disable="disabled"
                /></q-item-section>
              </q-item>
            </q-list>
            <q-btn
              class="w-100 q-mt-md"
              color="primary"
              label="Save"
              @click="updateBp"
              :disable="disabled"
            />
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list>
  </q-drawer>
</template>

<script>
export default {
  data: function () {
    return {
      beforeB: false,
      afterB: false,
    };
  },
  props: {
    opened: false,
    currentNode: null,
    breakpoints: {},
  },

  computed: {
    disabled: function () {
      return this.currentNode == null;
    },
  },

  watch: {
    currentNode: function (value) {
      let bp = this.breakpoints[this.currentNode];
      if (bp) {
        this.beforeB = bp.before;
        this.afterB = bp.after;
      } else {
        this.beforeB = false;
        this.afterB = false;
      }
    },
  },

  methods: {
    updateBp() {
      let state = {
        before: this.beforeB,
        after: this.afterB,
      };
      this.$emit("updateBp", state);
    },
  },
};
</script>