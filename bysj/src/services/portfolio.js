import { api } from './api';

export function getPortfolios(studentId) {
  return api({
    url: `/portfolio/${studentId}`,
    method: 'get'
  });
}

export function addPortfolio(data) {
  // Use FormData for file upload
  return api({
    url: '/portfolio',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function updatePortfolio(data) {
  // Use FormData for file upload
  return api({
    url: '/portfolio',
    method: 'put',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function deletePortfolio(id) {
  return api({
    url: `/portfolio/${id}`,
    method: 'delete'
  });
}

export function downloadPortfolio(id) {
  // 创建一个临时的a标签来触发下载
  const a = document.createElement('a');
  a.href = `${process.env.VUE_APP_API_BASE_URL || '/api'}/portfolio/download/${id}`;
  a.download = '';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}
