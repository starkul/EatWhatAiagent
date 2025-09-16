<template>
  <div class="chat-container">
    <div class="chat-header">
      <button @click="goBack" class="back-button">←</button>
      <h2>📅 AI 饮食规划超级智能体</h2>
      <div class="chat-id">会话 ID: {{ chatId }}</div>
    </div>
    
    <div class="chat-messages" ref="chatMessages">
      <div v-for="message in messages" :key="message.id" 
           :class="['message', message.sender === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-content">
          <div class="message-icon">{{ message.sender === 'user' ? '👤' : '🤖' }}</div>
          <div class="message-text">{{ message.content }}</div>
        </div>
        <div class="message-time">{{ formatTime(message.timestamp) }}</div>
      </div>
      
      <div v-if="loading" class="typing-indicator">
        <div class="message-content">
          <div class="message-icon">🤖</div>
          <div class="typing-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        v-model="userInput" 
        @keyup.enter="sendMessage" 
        placeholder="输入您的饮食规划需求..."
        :disabled="loading"
      >
      <button @click="sendMessage" :disabled="!userInput.trim() || loading">
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const chatMessages = ref(null);
const userInput = ref('');
const messages = ref([]);
const loading = ref(false);
const chatId = ref('');
let sseEmitter = null;

// 生成唯一的聊天ID
const generateChatId = () => {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessages.value) {
      chatMessages.value.scrollTop = chatMessages.value.scrollHeight;
    }
  });
};

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

// 发送消息
const sendMessage = async () => {
  const message = userInput.value.trim();
  if (!message || loading.value) return;
  
  // 添加用户消息
  messages.value.push({
    id: `user_${Date.now()}`,
    sender: 'user',
    content: message,
    timestamp: new Date().toISOString()
  });
  
  userInput.value = '';
  scrollToBottom();
  loading.value = true;
  
  try {
    // 调用SSE接口
    await startSSE(message);
  } catch (error) {
    console.error('发送消息失败:', error);
    messages.value.push({
      id: `error_${Date.now()}`,
      sender: 'ai',
      content: '抱歉，连接服务器失败，请稍后重试。',
      timestamp: new Date().toISOString()
    });
    loading.value = false;
    scrollToBottom();
  }
};

// 启动SSE连接
const startSSE = (message) => {
  return new Promise((resolve, reject) => {
    // 关闭之前的连接
    if (sseEmitter) {
      sseEmitter.close();
    }
    
    const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/ai/manus/chat?message=${encodeURIComponent(message)}`;
    
    try {
      // 创建EventSource连接
      sseEmitter = new EventSource(apiUrl);
      let fullMessageContent = ''; // 完整的消息内容
      let displayedContent = ''; // 当前显示的内容
      let aiMessageId = `ai_${Date.now()}`;
      let typewriterTimer = null;
      
      // 添加空的AI消息占位符
      messages.value.push({
        id: aiMessageId,
        sender: 'ai',
        content: '',
        timestamp: new Date().toISOString()
      });
      scrollToBottom();
      
      // 打字机效果函数
      const typewriterEffect = () => {
        const messageIndex = messages.value.findIndex(m => m.id === aiMessageId);
        if (messageIndex >= 0 && displayedContent.length < fullMessageContent.length) {
          // 一次显示1-3个字符，增加打字机的自然感
          const charsToAdd = Math.min(Math.floor(Math.random() * 3) + 1, fullMessageContent.length - displayedContent.length);
          displayedContent = fullMessageContent.substring(0, displayedContent.length + charsToAdd);
          messages.value[messageIndex].content = displayedContent;
          scrollToBottom();
          
          // 设置下一次打字的时间间隔，模拟真实打字速度
          const delay = Math.random() * 50 + 30; // 30-80ms的随机延迟
          typewriterTimer = setTimeout(typewriterEffect, delay);
        }
      };
      
      // 监听消息事件
      sseEmitter.onmessage = (event) => {
        try {
          // 后端返回的是纯文本，直接使用
          const chunk = event.data;
          if (chunk) {
            // 先保存完整的消息内容
            fullMessageContent += chunk;
            
            // 清除之前的定时器，重新开始打字机效果
            if (typewriterTimer) {
              clearTimeout(typewriterTimer);
            }
            
            // 立即开始新一轮的打字机效果
            typewriterEffect();
          }
        } catch (error) {
          console.error('处理SSE消息失败:', error);
        }
      };
      
      // 监听连接打开
      sseEmitter.onopen = () => {
        console.log('SSE连接已打开');
      };
      
      // 监听错误事件
      sseEmitter.onerror = (error) => {
        console.error('SSE连接错误:', error);
        sseEmitter.close();
        
        // 如果是正常关闭（例如消息发送完毕），就不报错
        if (fullMessageContent) {
          loading.value = false;
          resolve();
        } else {
          reject(error);
        }
      };
      
      // 监听连接关闭
      sseEmitter.addEventListener('close', () => {
        console.log('SSE连接已关闭');
        loading.value = false;
        resolve();
      });
      
    } catch (error) {
      reject(error);
    }
  });
};

// 返回主页
const goBack = () => {
  if (sseEmitter) {
    sseEmitter.close();
  }
  router.push('/');
};

// 页面挂载时初始化
onMounted(() => {
  chatId.value = generateChatId();
  
  // 欢迎消息
  messages.value.push({
    id: `welcome_${Date.now()}`,
    sender: 'ai',
    content: '欢迎使用AI饮食规划超级智能体！请告诉我们您的饮食目标、偏好和限制，我会为您制定个性化的饮食计划。',
    timestamp: new Date().toISOString()
  });
  scrollToBottom();
});
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: 'Arial', sans-serif;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  margin-right: 1rem;
  color: #666;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: #f0f0f0;
  color: #333;
}

.chat-header h2 {
  flex: 1;
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.chat-id {
  font-size: 0.8rem;
  color: #999;
  background: #f5f5f5;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
}

.chat-messages {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message {
  display: flex;
  flex-direction: column;
  max-width: 70%;
  word-wrap: break-word;
}

.user-message {
  align-self: flex-end;
}

.ai-message {
  align-self: flex-start;
}

.message-content {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.message-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
  margin-top: 0.25rem;
}

.message-text {
  padding: 0.75rem 1rem;
  border-radius: 18px;
  line-height: 1.4;
  font-size: 0.95rem;
}

.user-message .message-text {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message .message-text {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-time {
  font-size: 0.7rem;
  color: #999;
  margin-top: 0.25rem;
  text-align: center;
}

.typing-indicator {
  align-self: flex-start;
  max-width: 70%;
}

.typing-dots {
  display: flex;
  gap: 0.25rem;
  padding: 0.75rem 1rem;
  background: white;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-dots span:nth-child(1) { animation-delay: -0.32s; }
.typing-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  display: flex;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  background: white;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  bottom: 0;
}

.chat-input input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 0.95rem;
  outline: none;
  transition: all 0.3s ease;
}

.chat-input input:focus {
  border-color: #a8edea;
  box-shadow: 0 0 0 3px rgba(168, 237, 234, 0.1);
}

.chat-input input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.chat-input button {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.chat-input button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(168, 237, 234, 0.3);
}

.chat-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .message {
    max-width: 85%;
  }
  
  .chat-header {
    padding: 0.75rem 1rem;
  }
  
  .chat-messages {
    padding: 1rem;
  }
  
  .chat-input {
    padding: 0.75rem 1rem;
  }
}
</style>