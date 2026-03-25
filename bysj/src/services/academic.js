import { api } from './api';

export function getAcademicRecords(studentId) {
  return api({
    url: `/academic/${studentId}`,
    method: 'get'
  });
}

export function addAcademicRecord(data) {
  return api({
    url: '/academic',
    method: 'post',
    data
  });
}

export function updateAcademicRecord(data) {
  return api({
    url: '/academic',
    method: 'put',
    data
  });
}

export function deleteAcademicRecord(id) {
  return api({
    url: `/academic/${id}`,
    method: 'delete'
  });
}
