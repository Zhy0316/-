import { api } from './api';

export function getAwards(studentId) {
  return api({
    url: `/award/${studentId}`,
    method: 'get'
  });
}

export function addAward(data) {
  // Use FormData for file upload
  return api({
    url: '/award',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function updateAward(data) {
  // Use FormData for file upload
  return api({
    url: '/award',
    method: 'put',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export function deleteAward(id) {
  return api({
    url: `/award/${id}`,
    method: 'delete'
  });
}
