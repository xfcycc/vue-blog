<template>
  <el-card class="main-card">
    <div class="title">游戏管理</div>
    <div class="operation-container game-toolbar">
      <div>
        <el-button
          type="primary"
          size="small"
          icon="el-icon-plus"
          @click="addGame"
        >
          新增游戏
        </el-button>
        <el-button
          type="danger"
          size="small"
          icon="el-icon-delete"
          :disabled="gameIdList.length === 0"
          @click="deleteFlag = true"
        >
          删除
        </el-button>
        <el-button
          type="success"
          size="small"
          icon="el-icon-refresh"
          :loading="syncing"
          @click="syncSteam"
        >
          同步 Steam
        </el-button>
      </div>
      <div class="game-filters">
        <el-select
          v-model="query.platform"
          clearable
          size="small"
          placeholder="平台"
          @change="searchGames"
        >
          <el-option
            v-for="item in platformOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="query.playStatus"
          clearable
          size="small"
          placeholder="状态"
          @change="searchGames"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="query.isVisible"
          clearable
          size="small"
          placeholder="公开状态"
          @change="searchGames"
        >
          <el-option label="公开" :value="1" />
          <el-option label="隐藏" :value="0" />
        </el-select>
        <el-input
          v-model="query.keywords"
          prefix-icon="el-icon-search"
          size="small"
          placeholder="搜索游戏名称"
          @keyup.enter.native="searchGames"
        />
        <el-button
          type="primary"
          size="small"
          icon="el-icon-search"
          @click="searchGames"
        >
          搜索
        </el-button>
      </div>
    </div>

    <el-table
      border
      :data="gameList"
      v-loading="loading"
      @selection-change="selectionChange"
    >
      <el-table-column type="selection" width="48" :selectable="canDelete" />
      <el-table-column label="封面" align="center" width="132">
        <template slot-scope="scope">
          <img
            v-if="scope.row.cover"
            :src="scope.row.cover"
            class="game-cover"
          />
          <span v-else>暂无封面</span>
        </template>
      </el-table-column>
      <el-table-column prop="gameName" label="游戏" min-width="180" />
      <el-table-column label="平台" align="center" min-width="150">
        <template slot-scope="scope">
          <el-tag
            v-for="item in scope.row.platformList"
            :key="item"
            size="mini"
            class="platform-tag"
          >
            {{ platformName(item) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template slot-scope="scope">{{
          statusName(scope.row.playStatus)
        }}</template>
      </el-table-column>
      <el-table-column label="累计时长" align="center" width="100">
        <template slot-scope="scope">{{
          formatPlaytime(scope.row.playtimeForever)
        }}</template>
      </el-table-column>
      <el-table-column label="公开" align="center" width="75">
        <template slot-scope="scope">
          <el-tag
            size="mini"
            :type="scope.row.isVisible === 1 ? 'success' : 'info'"
          >
            {{ scope.row.isVisible === 1 ? "是" : "否" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="sortOrder"
        label="排序"
        align="center"
        width="75"
      />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="editGame(scope.row.id)"
            >编辑</el-button
          >
          <el-button
            v-if="scope.row.source === 'CUSTOM'"
            type="danger"
            size="mini"
            @click="deleteOne(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination-container"
      background
      :current-page="query.current"
      :page-size="query.size"
      :total="count"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="sizeChange"
      @current-change="currentChange"
    />

    <el-dialog :visible.sync="deleteFlag" width="30%">
      <div class="dialog-title-container" slot="title">
        <i class="el-icon-warning" style="color:#ff9900" />提示
      </div>
      <div>确定删除选中的自定义游戏吗？</div>
      <div slot="footer">
        <el-button @click="deleteFlag = false">取消</el-button>
        <el-button type="primary" @click="deleteGames(gameIdList)"
          >确定</el-button
        >
      </div>
    </el-dialog>
  </el-card>
</template>

<script>
export default {
  created() {
    this.listGames();
  },
  data() {
    return {
      loading: false,
      syncing: false,
      deleteFlag: false,
      gameIdList: [],
      gameList: [],
      count: 0,
      query: {
        current: 1,
        size: 10,
        keywords: "",
        platform: "",
        playStatus: "",
        isVisible: null
      },
      statusOptions: [
        { label: "想玩", value: "WANT" },
        { label: "在玩", value: "PLAYING" },
        { label: "玩过", value: "PLAYED" }
      ],
      platformOptions: [
        { label: "Steam", value: "STEAM" },
        { label: "PSN", value: "PSN" },
        { label: "PC", value: "PC" },
        { label: "手游", value: "MOBILE" },
        { label: "Switch", value: "SWITCH" }
      ],
      platformNames: {
        STEAM: "Steam",
        PSN: "PSN",
        PC: "PC",
        MOBILE: "手游",
        SWITCH: "Switch"
      }
    };
  },
  methods: {
    listGames() {
      this.loading = true;
      this.axios
        .post("/api/admin/games/list", this.query)
        .then(({ data }) => {
          if (data.flag && data.data) {
            this.gameList = data.data.recordList || [];
            this.count = data.data.count || 0;
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    searchGames() {
      this.query.current = 1;
      this.listGames();
    },
    sizeChange(size) {
      this.query.size = size;
      this.listGames();
    },
    currentChange(current) {
      this.query.current = current;
      this.listGames();
    },
    selectionChange(gameList) {
      this.gameIdList = gameList
        .filter(item => item.source === "CUSTOM")
        .map(item => item.id);
    },
    canDelete(row) {
      return row.source === "CUSTOM";
    },
    addGame() {
      this.$router.push({ path: "/games/edit/new" });
    },
    editGame(id) {
      this.$router.push({ path: "/games/edit/" + id });
    },
    deleteOne(id) {
      this.$confirm("确定删除这个自定义游戏吗？", "提示", { type: "warning" })
        .then(() => this.deleteGames([id]))
        .catch(() => {});
    },
    deleteGames(idList) {
      this.axios
        .post("/api/admin/games/delete", { idList })
        .then(({ data }) => {
          if (data.flag) {
            const result = data.data || {};
            this.$notify.success({
              title: "成功",
              message: "已删除 " + (result.deletedCount || 0) + " 个游戏"
            });
            this.deleteFlag = false;
            this.gameIdList = [];
            this.listGames();
          }
        });
    },
    syncSteam() {
      this.syncing = true;
      this.axios
        .post("/api/admin/games/steam/sync", { includePlayedFreeGames: true })
        .then(({ data }) => {
          if (data.flag) {
            const result = data.data || {};
            this.$notify.success({
              title: "Steam 同步完成",
              message:
                "新增 " +
                (result.addedCount || 0) +
                "，更新 " +
                (result.updatedCount || 0) +
                "，跳过 " +
                (result.skippedCount || 0)
            });
            this.listGames();
          } else {
            this.$notify.error({ title: "同步失败", message: data.message });
          }
        })
        .finally(() => {
          this.syncing = false;
        });
    },
    statusName(value) {
      const item = this.statusOptions.find(status => status.value === value);
      return item ? item.label : "未设置";
    },
    platformName(value) {
      return this.platformNames[value] || value;
    },
    formatPlaytime(minutes) {
      if (!minutes) return "0 小时";
      return Math.round(minutes / 60) + " 小时";
    }
  }
};
</script>

<style scoped>
.game-toolbar {
  justify-content: space-between;
  gap: 16px;
}
.game-filters {
  display: flex;
  gap: 10px;
}
.game-filters .el-select {
  width: 112px;
}
.game-filters .el-input {
  width: 190px;
}
.game-cover {
  display: block;
  width: 108px;
  height: 50px;
  margin: 0 auto;
  border-radius: 5px;
  object-fit: cover;
}
.platform-tag {
  margin: 2px;
}
@media (max-width: 1200px) {
  .game-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
  .game-filters {
    flex-wrap: wrap;
  }
}
</style>
