import { api } from './api';

export const forumAPI = {
  getPostList() {
    return api.get('/forum/posts')
  },

  getPostDetail(postId) {
    return api.get(`/forum/post/${postId}`)
  },

  getUserPosts() {
    return api.get('/forum/user/posts')
  },

  getUserCollections() {
    return api.get('/forum/user/collections')
  },

  getUserLikedPosts() {
    return api.get('/forum/user/liked')
  },

  searchPosts(keyword) {
    return api.get('/forum/search', { keyword })
  },

  createPost(data) {
    return api.post('/forum/post', data)
  },

  updatePost(postId, data) {
    return api.put(`/forum/post/${postId}`, data)
  },

  deletePost(postId) {
    return api.delete(`/forum/post/${postId}`)
  },

  likePost(postId) {
    return api.post(`/forum/like/${postId}`)
  },

  unlikePost(postId) {
    return api.delete(`/forum/like/${postId}`)
  },

  collectPost(postId) {
    return api.post(`/forum/collect/${postId}`)
  },

  uncollectPost(postId) {
    return api.delete(`/forum/collect/${postId}`)
  },

  getComments(postId) {
    return api.get(`/forum/comment/${postId}`)
  },

  addComment(postId, content, parentId = null) {
    return api.post(`/forum/comment/${postId}`, { content, parentId })
  },

  deleteComment(commentId) {
    return api.delete(`/forum/comment/${commentId}`)
  },

  likeComment(commentId) {
    return api.post(`/forum/comment/${commentId}/like`)
  },

  uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/forum/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
