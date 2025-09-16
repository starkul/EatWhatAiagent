<template>
  <div class="home-container">
    <div class="header">
      <h1>🍽️ AI 智能饮食助手</h1>
      <p>欢迎使用我们的AI饮食助手，帮助您解决饮食选择和健康规划的问题</p>
    </div>
    
    <div class="app-cards">
      <router-link to="/eat-app" class="app-card eat-app-card">
        <div class="app-icon">🍳</div>
        <h2>吃什么？饮食推荐大师</h2>
        <p>获取个性化的饮食推荐，解决您的用餐困扰</p>
        <div class="app-button">立即开始</div>
      </router-link>
      
      <router-link to="/manus" class="app-card manus-card">
        <div class="app-icon">📅</div>
        <h2>AI 饮食规划超级智能体</h2>
        <p>智能规划您的饮食，帮助您实现健康目标</p>
        <div class="app-button">立即开始</div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
// 主页组件，用于切换不同的应用
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Arial', sans-serif;
  padding: 2rem;
  box-sizing: border-box;
}

.header {
  text-align: center;
  margin-bottom: 3rem;
}

.header h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 1rem;
  font-weight: 700;
}

.header p {
  font-size: 1.1rem;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.app-cards {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
  justify-content: center;
}

.app-card {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  text-align: center;
  text-decoration: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.app-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
}

.app-card h2 {
  font-size: 1.4rem;
  color: #333;
  margin-bottom: 1rem;
  font-weight: 600;
}

.app-card p {
  font-size: 1rem;
  color: #666;
  margin-bottom: 2rem;
  line-height: 1.5;
}

.app-button {
  display: inline-block;
  padding: 0.8rem 1.5rem;
  background: #4CAF50;
  color: white;
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.app-button:hover {
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .header h1 {
    font-size: 2rem;
  }
  
  .app-cards {
    flex-direction: column;
    align-items: center;
  }
  
  .app-card {
    width: 100%;
    max-width: 350px;
  }
}
</style>