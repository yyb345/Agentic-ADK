import { Provide } from '@midwayjs/core';
import { InjectEntityModel } from '@midwayjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../entities/user';

@Provide()
export class UserService {
  @InjectEntityModel(User)
  userModel: Repository<User>;

  // todo
}
