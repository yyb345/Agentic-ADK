import { User } from './entities/user';

export interface CenterUser {
  id: string;
  email: string;
}

export type UserInfo = CenterUser & User;
