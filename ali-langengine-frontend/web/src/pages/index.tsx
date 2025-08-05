import { useModel } from '@umijs/max';

export default () => {
  const { user } = useModel('user');
  return (
    <div className="m-10">
      <div>Home Page. ✨</div>
      <div>Current user: {user?.email}</div>
    </div>
  );
};
