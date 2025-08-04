import '@umijs/max/typings';

declare global {
  interface Window {
    user: UserInfo;
  }

  type UserInfo = CenterUser & User;

  interface CenterUser {
    id: string;
    email: string;
  }

  interface User {
    status: number;
  }
}
