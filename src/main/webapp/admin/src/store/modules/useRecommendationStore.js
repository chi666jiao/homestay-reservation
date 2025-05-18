import { defineStore } from 'pinia';
import { getRecommendations } from '@/api/recommendation';

export const useRecommendationStore = defineStore('recommendation', {
    state: () => ({
        list: [],
        loading: false,
        error: null
    }),
    actions: {
        async fetchRecommendations(params) {
            this.loading = true;
            try {
                const data = await getRecommendations(params);
                console.log('推荐数据:', data); // 查看控制台输出
                this.list = data;
                this.error = null;
            } catch (err) {
                this.error = err.message;
            } finally {
                this.loading = false;
            }
        }
    }
});