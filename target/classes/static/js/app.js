// ==================== 全局配置 ====================
const API_BASE = '';
const TOKEN_KEY = 'blog_token';
const USER_KEY = 'blog_user';

// ==================== 工具函数 ====================
const Utils = {
    // 获取Token
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },
    
    // 设置Token
    setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },
    
    // 移除Token
    removeToken() {
        localStorage.removeItem(TOKEN_KEY);
    },
    
    // 获取用户信息
    getUser() {
        const user = localStorage.getItem(USER_KEY);
        return user ? JSON.parse(user) : null;
    },
    
    // 设置用户信息
    setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    
    // 移除用户信息
    removeUser() {
        localStorage.removeItem(USER_KEY);
    },
    
    // 检查是否登录
    isLoggedIn() {
        return !!this.getToken();
    },
    
    // 格式化日期
    formatDate(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    },
    
    // 格式化日期时间
    formatDateTime(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day} ${hour}:${minute}`;
    },
    
    // 相对时间
    timeAgo(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const now = new Date();
        const diff = now - date;
        const seconds = Math.floor(diff / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);
        
        if (days > 30) return this.formatDate(dateStr);
        if (days > 0) return `${days}天前`;
        if (hours > 0) return `${hours}小时前`;
        if (minutes > 0) return `${minutes}分钟前`;
        return '刚刚';
    },
    
    // 截取摘要
    getSummary(content, length = 200) {
        if (!content) return '';
        // 移除HTML标签
        const text = content.replace(/<[^>]+>/g, '');
        return text.length > length ? text.substring(0, length) + '...' : text;
    },
    
    // 防抖
    debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }
};

// ==================== API请求 ====================
const Api = {
    // 通用请求方法
    async request(url, options = {}) {
        const token = Utils.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        try {
            const response = await fetch(API_BASE + url, {
                ...options,
                headers
            });
            
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API请求错误:', error);
            throw error;
        }
    },
    
    // GET请求
    get(url) {
        return this.request(url);
    },
    
    // POST请求
    post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },
    
    // PUT请求
    put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },
    
    // DELETE请求
    delete(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }
};

// ==================== 消息提示 ====================
const Toast = {
    show(message, type = 'success', duration = 3000) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.style.animation = 'slideIn 0.3s ease reverse';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },
    
    success(message) {
        this.show(message, 'success');
    },
    
    error(message) {
        this.show(message, 'error');
    },
    
    warning(message) {
        this.show(message, 'warning');
    }
};

// ==================== 用户认证 ====================
const Auth = {
    // 登录
    async login(credential, password) {
        const result = await Api.post('/api/auth/login', { credential, password });
        if (result.success) {
            Utils.setToken(result.data.token);
            await this.fetchUserInfo();
        }
        return result;
    },
    
    // 邮箱验证码登录
    async loginByCaptcha(email, captcha) {
        const result = await Api.post('/api/auth/login/captcha', { email, captcha });
        if (result.success) {
            Utils.setToken(result.data.token);
            await this.fetchUserInfo();
        }
        return result;
    },

    // 重置密码
    async resetPassword(email, captcha, password) {
        return await Api.post('/api/auth/reset-password', { email, captcha, password });
    },
    
    // 注册
    async register(username, email, password, captcha) {
        return await Api.post('/api/auth/register', { username, email, password, captcha });
    },
    
    // 发送验证码
    async sendCaptcha(email, type) {
        return await Api.post('/api/auth/send-captcha', { email, type });
    },
    
    // 获取用户信息
    async fetchUserInfo() {
        const result = await Api.get('/api/auth/check');
        if (result.success) {
            Utils.setUser(result.data);
        }
        return result;
    },
    
    // 登出
    async logout() {
        await Api.post('/api/auth/logout');
        Utils.removeToken();
        Utils.removeUser();
        window.location.href = '/';
    },
    
    // 检查登录状态
    async checkAuth() {
        if (!Utils.isLoggedIn()) {
            return false;
        }
        const result = await this.fetchUserInfo();
        return result.success;
    }
};

// ==================== 文章相关 ====================
const Article = {
    // 获取文章列表
    async getList(page = 0, size = 10, status = null) {
        let url = `/api/articles?page=${page}&size=${size}`;
        if (status !== null) url += `&status=${status}`;
        return await Api.get(url);
    },
    
    // 获取文章详情
    async getDetail(id) {
        return await Api.get(`/api/articles/${id}`);
    },
    
    // 创建文章
    async create(article) {
        return await Api.post('/api/articles', article);
    },
    
    // 更新文章
    async update(id, article) {
        return await Api.put(`/api/articles/${id}`, article);
    },
    
    // 删除文章
    async delete(id) {
        return await Api.delete(`/api/articles/${id}`);
    },
    
    // 搜索文章
    async search(keyword, page = 0, size = 10) {
        return await Api.get(`/api/articles/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`);
    },
    
    // 点赞文章
    async toggleLike(id) {
        return await Api.post(`/api/articles/${id}/like`);
    },
    
    // 获取点赞状态
    async getLikeStatus(id) {
        return await Api.get(`/api/articles/${id}/like/status`);
    }
};

// ==================== 评论相关 ====================
const Comment = {
    // 获取文章评论
    async getByArticle(articleId) {
        return await Api.get(`/api/comments/tree/article/${articleId}`);
    },
    
    // 创建评论
    async create(comment) {
        return await Api.post('/api/comments', comment);
    },
    
    // 删除评论
    async delete(id) {
        return await Api.delete(`/api/comments/${id}`);
    }
};

// ==================== 分类相关 ====================
const Category = {
    // 获取所有分类
    async getAll() {
        return await Api.get('/api/categories');
    },
    
    // 获取分类详情
    async getById(id) {
        return await Api.get(`/api/categories/${id}`);
    }
};

// ==================== 标签相关 ====================
const Tag = {
    // 获取所有标签
    async getAll() {
        return await Api.get('/api/tags');
    },
    
    // 获取标签详情
    async getById(id) {
        return await Api.get(`/api/tags/${id}`);
    }
};

// ==================== 页面初始化 ====================
document.addEventListener('DOMContentLoaded', async () => {
    // 检查URL中是否有token参数（OAuth回调）
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
        Utils.setToken(token);
        await Auth.fetchUserInfo();
        // 移除URL中的token参数
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    // 更新导航栏用户状态
    updateNavbar();
    
    // 绑定用户下拉菜单
    bindUserDropdown();
    
    // 绑定搜索功能
    bindSearch();
});

// 更新导航栏
function updateNavbar() {
    const authButtons = document.querySelector('.auth-buttons');
    const userMenu = document.querySelector('.user-menu');
    
    if (!authButtons || !userMenu) return;
    
    if (Utils.isLoggedIn()) {
        const user = Utils.getUser();
        authButtons.classList.add('hidden');
        userMenu.classList.remove('hidden');
        
        // 更新头像
        const avatar = userMenu.querySelector('.user-avatar');
        if (avatar && user) {
            avatar.src = user.avatar || '/images/default-avatar.svg';
            avatar.alt = user.username;
        }
        
        // 控制管理后台链接显示
        const adminLink = userMenu.querySelector('.admin-link');
        if (adminLink) {
            if (user && user.role === 'ADMIN') {
                adminLink.classList.remove('hidden');
            } else {
                adminLink.classList.add('hidden');
            }
        }
    } else {
        authButtons.classList.remove('hidden');
        userMenu.classList.add('hidden');
    }
}

// 绑定用户下拉菜单
function bindUserDropdown() {
    const userMenu = document.querySelector('.user-menu');
    if (!userMenu) return;
    
    const avatar = userMenu.querySelector('.user-avatar');
    const dropdown = userMenu.querySelector('.user-dropdown');
    
    if (avatar && dropdown) {
        avatar.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });
        
        document.addEventListener('click', () => {
            dropdown.classList.remove('show');
        });
    }
    
    // 绑定登出按钮
    const logoutBtn = document.querySelector('.logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            Auth.logout();
        });
    }
}

// 绑定搜索功能
function bindSearch() {
    const searchForm = document.querySelector('.search-box');
    if (!searchForm) return;
    
    const searchInput = searchForm.querySelector('input');
    const searchBtn = searchForm.querySelector('button');
    
    const doSearch = () => {
        const keyword = searchInput.value.trim();
        if (keyword) {
            window.location.href = `/search?keyword=${encodeURIComponent(keyword)}`;
        }
    };
    
    if (searchBtn) {
        searchBtn.addEventListener('click', (e) => {
            e.preventDefault();
            doSearch();
        });
    }
    
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                doSearch();
            }
        });
    }
}

// 渲染分页
function renderPagination(container, currentPage, totalPages, baseUrl) {
    if (!container || totalPages <= 1) return;
    
    let html = '';
    
    // 上一页
    if (currentPage > 0) {
        html += `<a href="${baseUrl}?page=${currentPage - 1}"><i class="fas fa-chevron-left"></i></a>`;
    } else {
        html += `<span class="disabled"><i class="fas fa-chevron-left"></i></span>`;
    }
    
    // 页码
    const start = Math.max(0, currentPage - 2);
    const end = Math.min(totalPages - 1, currentPage + 2);
    
    if (start > 0) {
        html += `<a href="${baseUrl}?page=0">1</a>`;
        if (start > 1) html += `<span>...</span>`;
    }
    
    for (let i = start; i <= end; i++) {
        if (i === currentPage) {
            html += `<span class="active">${i + 1}</span>`;
        } else {
            html += `<a href="${baseUrl}?page=${i}">${i + 1}</a>`;
        }
    }
    
    if (end < totalPages - 1) {
        if (end < totalPages - 2) html += `<span>...</span>`;
        html += `<a href="${baseUrl}?page=${totalPages - 1}">${totalPages}</a>`;
    }
    
    // 下一页
    if (currentPage < totalPages - 1) {
        html += `<a href="${baseUrl}?page=${currentPage + 1}"><i class="fas fa-chevron-right"></i></a>`;
    } else {
        html += `<span class="disabled"><i class="fas fa-chevron-right"></i></span>`;
    }
    
    container.innerHTML = html;
}

// 导出到全局
window.Utils = Utils;
window.Api = Api;
window.Toast = Toast;
window.Auth = Auth;
window.Article = Article;
window.Comment = Comment;
window.Category = Category;
window.Tag = Tag;
window.renderPagination = renderPagination;
