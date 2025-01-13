export const getCommentStatus = (approved: any) => {
  return approved === 1 || approved === true || approved === '0x01' || approved > 0 
    ? 'approved' 
    : 'pending';
}; 