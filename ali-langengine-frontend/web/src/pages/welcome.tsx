import { useModel } from '@umijs/max';

export default () => {
  const { user } = useModel('user');
  return (
    <div className="m-10">
      <div>Welcome Page. ✨</div>
      <div>Current user: {user?.email}</div>
    </div>
  );
};
