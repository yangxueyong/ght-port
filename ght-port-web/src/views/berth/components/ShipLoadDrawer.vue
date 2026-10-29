<template>
  <el-drawer :model-value="modelValue" size="760px" :title="`分派船只 · ${berth?.berthName || ''}`" @update:model-value="emit('update:modelValue', $event)" @open="init">
    <div class="assign-box">
      <div class="section-title">选择装船货物与船只</div>
      <el-form label-width="78px" class="mt-12">
        <el-form-item label="装船货物">
          <el-radio-group v-if="loadCargos.length" v-model="form.cargoId" class="cargo-radios">
            <el-radio-button v-for="c in loadCargos" :key="c.id" :value="c.id">{{ c.cargoName }}</el-radio-button>
          </el-radio-group>
          <span v-else class="muted small">暂无待装船货物，请先点击“泊位装货”登记货物</span>
        </el-form-item>
        <el-form-item v-if="current" label="剩余">
          <span class="remain num">{{ fmtNum(current.tonnage - current.loadedTonnage, 3) }} 吨</span>
          <span class="muted small">（共 {{ fmtNum(current.tonnage, 3) }} 吨，已装 {{ fmtNum(current.loadedTonnage, 3) }} 吨）</span>
        </el-form-item>
        <el-form-item label="船只">
          <el-select v-model="form.shipIds" popper-class="ght-rich-option" multiple filterable remote :remote-method="searchShips" :loading="shipLoading"
            placeholder="输入船名或船运公司搜索船只（可多选）" style="width: 100%" @visible-change="(v) => v && !ships.length && searchShips('')">
            <el-option v-for="s in ships" :key="s.id" :label="`${s.companyName} / ${s.shipName}`" :value="s.id">
              <div class="ship-opt">
                <ShipSvg :type="s.shipType" :length="s.shipLength" :color="s.hullColor || '#1f6fd1'" :width="56" />
                <div>
                  <div>{{ s.shipName }} <span class="muted">· {{ s.shipTypeDesc }}</span></div>
                  <div class="muted small">{{ s.companyName }}</div>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <div class="assign-actions">
          <span class="muted small">提示：分派后在下方填写每艘船的装船量并点击“装船完成”</span>
          <el-button type="primary" icon="Check" :loading="assigning" :disabled="!loadCargos.length" @click="assign">确定</el-button>
        </div>
      </el-form>
    </div>

    <div class="flex-between mt-16">
      <div class="section-title">分派的船只（{{ filtered.length }}）</div>
      <el-radio-group v-model="filter" size="small">
        <el-radio-button value="ALL">全部</el-radio-button>
        <el-radio-button value="WAITING">待装船</el-radio-button>
        <el-radio-button value="LOADED">已装船</el-radio-button>
      </el-radio-group>
    </div>
    <div class="mt-12">
      <ShipLoadList :loads="filtered" :can-edit="canEdit" empty-text="请在上方选择船只后点击“确定”" @changed="reload" />
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ShipSvg from '@/components/svg/ShipSvg.vue'
import ShipLoadList from './ShipLoadList.vue'
import { fmtNum } from '@/utils/format'
import { shipOptions } from '@/api/shipper'
import { shipLoadAssign, shipLoadList } from '@/api/berth'
import { useUserStore } from '@/stores/user'

const props = defineProps({ modelValue: Boolean, berth: Object, cargos: { type: Array, default: () => [] }, cargoId: String })
const emit = defineEmits(['update:modelValue', 'changed'])
const store = useUserStore()
const canEdit = computed(() => store.hasPerm('berth:cargo:load'))

const form = reactive({ cargoId: null, shipIds: [] })
const ships = ref([])
const shipLoading = ref(false)
const assigning = ref(false)
const list = ref([])
const filter = ref('ALL')
const loadCargos = computed(() => props.cargos.filter((c) => c.direction === 'LOAD'))
const current = computed(() => loadCargos.value.find((c) => c.id === form.cargoId))
const filtered = computed(() => list.value.filter((l) => l.cargoId === form.cargoId && (filter.value === 'ALL' || l.status === filter.value)))

function init() {
  form.cargoId = (loadCargos.value.find((c) => c.id === props.cargoId) || loadCargos.value[0])?.id || null
  form.shipIds = []
  reload(false)
}
async function reload(notify = true) {
  if (!props.berth) return
  list.value = await shipLoadList({ berthId: props.berth.id, statuses: ['WAITING', 'LOADED'] })
  if (notify) emit('changed')
}
async function searchShips(q) {
  shipLoading.value = true
  try {
    const res = await shipOptions(q)
    const keep = ships.value.filter((s) => form.shipIds.includes(s.id) && !res.find((x) => x.id === s.id))
    ships.value = [...keep, ...res]
  } finally {
    shipLoading.value = false
  }
}
async function assign() {
  if (!form.cargoId) return ElMessage.warning('请选择装船货物')
  if (!form.shipIds.length) return ElMessage.warning('请选择船只')
  assigning.value = true
  try {
    const added = await shipLoadAssign({ berthId: props.berth.id, ...form })
    ElMessage.success(`已分派 ${added.length} 艘船只，请填写装船量`)
    form.shipIds = []
    filter.value = 'ALL'
    await reload()
  } finally {
    assigning.value = false
  }
}
</script>

<style scoped lang="scss">
.assign-box { padding: 14px 16px; border-radius: 14px; background: linear-gradient(180deg, #f6f3ff, #fff); border: 1px solid #e4dcfa; }
.cargo-radios { flex-wrap: wrap; }
.ship-opt { display: flex; align-items: center; gap: 10px; line-height: 1.3; padding: 2px 0; }
.remain { font-weight: 700; color: #7c3aed; margin-right: 6px; }
.small { font-size: 12px; }
.assign-actions { display: flex; align-items: center; justify-content: space-between; }
</style>
