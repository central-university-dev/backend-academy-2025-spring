"""delete email column

Revision ID: 53654f8d12c0
Revises: 2fbff4725664
Create Date: 2025-03-19 19:32:00.592124

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '53654f8d12c0'
down_revision: Union[str, None] = '2fbff4725664'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_column('users', 'email')
    # ### end Alembic commands ###


def downgrade() -> None:
    """Downgrade schema."""
    # ### commands auto generated by Alembic - please adjust! ###
    op.add_column('users', sa.Column('email', sa.VARCHAR(), nullable=False, server_default="UNK"))
    # ### end Alembic commands ###
