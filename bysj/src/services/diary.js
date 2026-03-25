import { api } from './api';

export function getDiaries(studentId, params) {
  return api({
    url: '/diary',
    method: 'get',
    params: {
      studentId,
      ...params
    }
  });
}

export function createDiary(data) {
  // Use FormData for file upload
  return api({
    url: '/diary',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}
