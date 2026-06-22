<template>
  <div class="filter-bar">
    <el-form inline @submit.prevent="emit('search')">
      <el-form-item label="搜索">
        <el-input
          :model-value="keyword"
          :placeholder="keywordPlaceholder"
          clearable
          class="filter-keyword"
          @update:model-value="updateKeyword"
          @keyup.enter="emit('search')"
        />
      </el-form-item>
      <el-form-item v-if="extraLabel" :label="extraLabel">
        <el-select
          :model-value="extraValue"
          clearable
          placeholder="全部"
          class="filter-select"
          @update:model-value="updateExtraValue"
        >
          <el-option
            v-for="option in extraOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          :model-value="status"
          clearable
          placeholder="全部状态"
          class="filter-select"
          @update:model-value="updateStatus"
        >
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="filter-actions">
        <el-button type="primary" :icon="Search" :loading="loading" native-type="submit">查询</el-button>
        <el-button :icon="RefreshLeft" :disabled="loading" @click="emit('reset')">清空</el-button>
      </el-form-item>
    </el-form>
    <span class="filter-count">共 {{ total }} 条</span>
  </div>
</template>

<script setup lang="ts">
import { RefreshLeft, Search } from '@element-plus/icons-vue'

export interface FilterOption {
  label: string
  value: string
}

withDefaults(defineProps<{
  keyword: string
  status: string
  keywordPlaceholder: string
  statusOptions: FilterOption[]
  total: number
  loading?: boolean
  extraLabel?: string
  extraValue?: string
  extraOptions?: FilterOption[]
}>(), {
  loading: false,
  extraLabel: '',
  extraValue: '',
  extraOptions: () => []
})

const emit = defineEmits<{
  'update:keyword': [value: string]
  'update:status': [value: string]
  'update:extraValue': [value: string]
  search: []
  reset: []
}>()

function updateKeyword(value: string) {
  emit('update:keyword', value)
}

function updateStatus(value?: string) {
  emit('update:status', value ?? '')
}

function updateExtraValue(value?: string) {
  emit('update:extraValue', value ?? '')
}
</script>
